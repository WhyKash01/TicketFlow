import React from 'react';
import { Ticket, MapPin, Search, Film, Calendar, ShieldCheck, LogIn, LogOut, User as UserIcon } from 'lucide-react';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { setSelectedCity } from '@/store/slices/citySlice';
import { openAuthModal, logout } from '@/store/slices/authSlice';
import { authApi } from '@/api/client';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

interface NavbarProps {
  onNavigate: (page: 'home' | 'movie-detail' | 'seat-booking' | 'my-bookings') => void;
  currentPage: string;
}

export default function Navbar({ onNavigate, currentPage }: NavbarProps) {
  const dispatch = useAppDispatch();
  const { selectedCity, availableCities } = useAppSelector((state) => state.city);
  const { userBookings } = useAppSelector((state) => state.booking);
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);

  const handleSignOut = async () => {
    try {
      await authApi.logout();
    } finally {
      dispatch(logout());
    }
  };

  return (
    <header className="sticky top-0 z-50 bg-[#0b0f19]/90 backdrop-blur-md border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 gap-4">
          
          {/* Logo */}
          <div 
            onClick={() => onNavigate('home')}
            className="flex items-center gap-2.5 cursor-pointer group"
          >
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-rose-600 to-amber-500 flex items-center justify-center shadow-lg shadow-rose-500/20 group-hover:scale-105 transition-transform">
              <Ticket className="w-6 h-6 text-white transform -rotate-12" />
            </div>
            <div>
              <span className="text-xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-100 to-rose-400 bg-clip-text text-transparent">
                Ticket<span className="text-rose-500">Flow</span>
              </span>
              <Badge variant="rose" className="hidden sm:inline-flex ml-2 text-[10px] tracking-wider uppercase font-semibold">
                TS + Redux
              </Badge>
            </div>
          </div>

          {/* Search bar */}
          <div className="hidden md:flex flex-1 max-w-md items-center relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5" />
            <input
              type="text"
              placeholder="Search for Movies, Events, Plays or Cinemas..."
              className="w-full bg-slate-900/90 text-sm text-slate-200 pl-10 pr-4 py-2 rounded-full border border-slate-800 focus:outline-none focus:border-rose-500 transition-colors placeholder:text-slate-500"
            />
          </div>

          {/* Right Controls */}
          <div className="flex items-center gap-3">
            {/* City Selector */}
            <div className="flex items-center gap-1.5 bg-slate-900 border border-slate-800 rounded-xl px-3 py-1.5 text-xs font-medium text-slate-300 hover:border-slate-700">
              <MapPin className="w-3.5 h-3.5 text-rose-500" />
              <select
                value={selectedCity}
                onChange={(e) => dispatch(setSelectedCity(e.target.value))}
                className="bg-transparent focus:outline-none cursor-pointer text-slate-200"
              >
                {availableCities.map(c => (
                  <option key={c} value={c} className="bg-slate-900 text-slate-200">
                    {c}
                  </option>
                ))}
              </select>
            </div>

            {/* My Tickets Button */}
            <Button
              variant={currentPage === 'my-bookings' ? 'default' : 'outline'}
              size="sm"
              onClick={() => onNavigate('my-bookings')}
              className="relative flex items-center gap-1.5 text-xs font-semibold"
            >
              <Ticket className="w-3.5 h-3.5" />
              <span>My Tickets</span>
              {userBookings.length > 0 && (
                <span className="ml-1 px-1.5 py-0.2 bg-rose-500 text-white rounded-full text-[10px]">
                  {userBookings.length}
                </span>
              )}
            </Button>

            {/* Auth Profile / Sign In */}
            {isAuthenticated && user ? (
              <div className="flex items-center gap-2 pl-1 border-l border-slate-800">
                <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 rounded-xl px-2.5 py-1.5 shadow-sm">
                  <div className="w-6 h-6 rounded-full bg-gradient-to-tr from-rose-500 to-amber-500 flex items-center justify-center text-white font-bold text-[11px]">
                    {user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
                  </div>
                  <span className="text-xs font-semibold text-slate-200 hidden sm:inline max-w-[110px] truncate">
                    {user.fullName || user.email}
                  </span>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleSignOut}
                  className="h-8 px-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10"
                  title="Sign Out"
                >
                  <LogOut className="w-4 h-4" />
                </Button>
              </div>
            ) : (
              <Button
                size="sm"
                onClick={() => dispatch(openAuthModal('login'))}
                className="bg-gradient-to-r from-rose-600 to-rose-500 hover:from-rose-500 hover:to-rose-400 text-white text-xs font-semibold px-3.5 shadow-md shadow-rose-600/20"
              >
                <LogIn className="w-3.5 h-3.5 mr-1.5" />
                <span>Sign In</span>
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* Categories sub-bar */}
      <div className="bg-slate-950/60 border-t border-slate-900 px-4">
        <div className="max-w-7xl mx-auto flex items-center space-x-6 text-xs font-medium text-slate-400 py-2 overflow-x-auto">
          <span 
            onClick={() => onNavigate('home')} 
            className="text-rose-400 font-semibold cursor-pointer flex items-center gap-1 hover:text-rose-300"
          >
            <Film className="w-3.5 h-3.5" /> Movies
          </span>
          <span className="cursor-pointer hover:text-white transition-colors flex items-center gap-1">
            <Calendar className="w-3.5 h-3.5" /> Live Events
          </span>
          <span className="cursor-pointer hover:text-white transition-colors">Plays & Theatre</span>
          <span className="cursor-pointer hover:text-white transition-colors">Sports</span>
          <span className="cursor-pointer hover:text-white transition-colors">Standup Comedy</span>
          <div className="ml-auto hidden lg:flex items-center gap-2 text-[11px] text-emerald-400 font-mono">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Redis Distributed Seat Locks Active</span>
          </div>
        </div>
      </div>
    </header>
  );
}

