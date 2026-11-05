import React, { useState } from 'react';
import { X, Lock, Mail, User as UserIcon, Phone, Loader2, Sparkles, AlertCircle, CheckCircle2 } from 'lucide-react';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import {
  closeAuthModal,
  setAuthModalTab,
  setCredentials,
  setAuthLoading,
  setAuthError,
} from '@/store/slices/authSlice';
import { authApi } from '@/api/client';
import { hashPasswordClient } from '@/lib/crypto';
import { Button } from '@/components/ui/button';

function extractErrorMessage(err: any, fallback: string): string {
  if (err.response?.data) {
    const data = err.response.data;
    if (typeof data === 'object') {
      if (data.message) return data.message;
      if (data.error) return data.error;
    } else if (typeof data === 'string') {
      try {
        const parsed = JSON.parse(data);
        if (parsed.message) return parsed.message;
        if (parsed.error) return parsed.error;
      } catch {
        return data;
      }
    }
  }
  return err.message || fallback;
}

export default function AuthModal() {
  const dispatch = useAppDispatch();
  const { isAuthModalOpen, authModalTab, isLoading, error } = useAppSelector((state) => state.auth);

  // Form states
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  if (!isAuthModalOpen) return null;

  const handleClose = () => {
    dispatch(closeAuthModal());
    setSuccessMessage(null);
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      dispatch(setAuthError('Please fill in both email and password.'));
      return;
    }

    dispatch(setAuthLoading(true));
    try {
      const encryptedPassword = await hashPasswordClient(password);
      const res = await authApi.login({ email, password: encryptedPassword });
      if (res.success && res.data) {
        const authData = res.data;
        dispatch(
          setCredentials({
            user: {
              id: authData.userId,
              email: authData.email,
              fullName: authData.fullName,
              role: authData.role,
            },
            accessToken: authData.accessToken,
            refreshToken: authData.refreshToken,
          })
        );
        setSuccessMessage('Welcome back! You are now logged in.');
        setTimeout(() => handleClose(), 700);
      } else {
        dispatch(setAuthError(res.message || 'Login failed. Please check your credentials.'));
      }
    } catch (err: any) {
      dispatch(setAuthError(extractErrorMessage(err, 'Login failed. Please check your credentials.')));
    } finally {
      dispatch(setAuthLoading(false));
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName || !email || !password) {
      dispatch(setAuthError('Full name, email, and password are required.'));
      return;
    }
    if (password.length < 6) {
      dispatch(setAuthError('Password must be at least 6 characters.'));
      return;
    }

    dispatch(setAuthLoading(true));
    try {
      const encryptedPassword = await hashPasswordClient(password);
      const res = await authApi.register({
        fullName,
        email,
        password: encryptedPassword,
        phoneNumber: phoneNumber || undefined,
      });

      if (res.success && res.data) {
        const authData = res.data;
        dispatch(
          setCredentials({
            user: {
              id: authData.userId,
              email: authData.email,
              fullName: authData.fullName,
              phoneNumber: phoneNumber || undefined,
              role: authData.role,
            },
            accessToken: authData.accessToken,
            refreshToken: authData.refreshToken,
          })
        );
        setSuccessMessage('Account created successfully! Welcome to TicketFlow.');
        setTimeout(() => handleClose(), 700);
      } else {
        dispatch(setAuthError(res.message || 'Registration failed.'));
      }
    } catch (err: any) {
      dispatch(setAuthError(extractErrorMessage(err, 'Registration failed. Email may already be in use.')));
    } finally {
      dispatch(setAuthLoading(false));
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
      <div 
        className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl shadow-rose-950/20 overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Top Decorative Banner */}
        <div className="relative h-28 bg-gradient-to-br from-rose-600 via-rose-500 to-amber-500 p-6 flex flex-col justify-end">
          <button
            onClick={handleClose}
            className="absolute top-4 right-4 p-1.5 rounded-full bg-black/30 hover:bg-black/50 text-white transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
          <div className="flex items-center gap-2 text-white">
            <Sparkles className="w-5 h-5 text-amber-200" />
            <h2 className="text-xl font-bold tracking-tight">
              {authModalTab === 'login' ? 'Sign In to TicketFlow' : 'Create an Account'}
            </h2>
          </div>
          <p className="text-xs text-rose-100/90 mt-0.5">
            {authModalTab === 'login' 
              ? 'Unlock instant seat reservations, digital QR passes, and booking history.' 
              : 'Join thousands of moviegoers & live event enthusiasts.'}
          </p>
        </div>

        {/* Tab Switcher */}
        <div className="flex border-b border-slate-800 bg-slate-950/60 p-1">
          <button
            type="button"
            onClick={() => dispatch(setAuthModalTab('login'))}
            className={`flex-1 py-2 text-xs font-semibold rounded-lg transition-all ${
              authModalTab === 'login'
                ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Sign In
          </button>
          <button
            type="button"
            onClick={() => dispatch(setAuthModalTab('register'))}
            className={`flex-1 py-2 text-xs font-semibold rounded-lg transition-all ${
              authModalTab === 'register'
                ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            New Account
          </button>
        </div>

        {/* Form Body */}
        <div className="p-6">
          {/* Error Banner */}
          {error && (
            <div className="mb-4 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 flex items-start gap-2.5 text-xs text-rose-300">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {/* Success Banner */}
          {successMessage && (
            <div className="mb-4 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center gap-2.5 text-xs text-emerald-300">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
              <span>{successMessage}</span>
            </div>
          )}

          {authModalTab === 'login' ? (
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Email Address
                </label>
                <div className="relative">
                  <Mail className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@domain.com"
                    className="w-full bg-slate-950/80 border border-slate-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 transition-all"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Password
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full bg-slate-950/80 border border-slate-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 transition-all"
                  />
                </div>
              </div>

              <Button
                type="submit"
                disabled={isLoading}
                className="w-full py-2.5 bg-gradient-to-r from-rose-600 to-rose-500 hover:from-rose-500 hover:to-rose-400 text-white font-semibold rounded-xl text-sm shadow-lg shadow-rose-600/25 transition-all mt-2"
              >
                {isLoading ? (
                  <span className="flex items-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin" /> Signing In...
                  </span>
                ) : (
                  'Sign In'
                )}
              </Button>

              <div className="text-center pt-2">
                <p className="text-xs text-slate-400">
                  Don't have an account?{' '}
                  <button
                    type="button"
                    onClick={() => dispatch(setAuthModalTab('register'))}
                    className="text-rose-400 hover:text-rose-300 font-semibold underline underline-offset-2 ml-1"
                  >
                    Create one here
                  </button>
                </p>
              </div>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="space-y-3.5">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Full Name
                </label>
                <div className="relative">
                  <UserIcon className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="Yash Sharma"
                    className="w-full bg-slate-950/80 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 transition-all"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Email Address
                </label>
                <div className="relative">
                  <Mail className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@domain.com"
                    className="w-full bg-slate-950/80 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 transition-all"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Password
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="password"
                    required
                    minLength={6}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Minimum 6 characters"
                    className="w-full bg-slate-950/80 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 transition-all"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Phone Number <span className="text-slate-500">(Optional)</span>
                </label>
                <div className="relative">
                  <Phone className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="tel"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    placeholder="+91 9876543210"
                    className="w-full bg-slate-950/80 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-sm text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 transition-all"
                  />
                </div>
              </div>

              <Button
                type="submit"
                disabled={isLoading}
                className="w-full py-2.5 bg-gradient-to-r from-rose-600 to-rose-500 hover:from-rose-500 hover:to-rose-400 text-white font-semibold rounded-xl text-sm shadow-lg shadow-rose-600/25 transition-all mt-2"
              >
                {isLoading ? (
                  <span className="flex items-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin" /> Registering...
                  </span>
                ) : (
                  'Create Account'
                )}
              </Button>

              <div className="text-center pt-2">
                <p className="text-xs text-slate-400">
                  Already have an account?{' '}
                  <button
                    type="button"
                    onClick={() => dispatch(setAuthModalTab('login'))}
                    className="text-rose-400 hover:text-rose-300 font-semibold underline underline-offset-2 ml-1"
                  >
                    Sign In
                  </button>
                </p>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

