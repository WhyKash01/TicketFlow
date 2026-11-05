import React, { useState } from 'react';
import { X, CreditCard, ArrowRight, RotateCcw, LogIn, CheckCircle2 } from 'lucide-react';
import { bookingApi, paymentApi } from '@/api/client';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { addConfirmedBooking, setSagaStep } from '@/store/slices/bookingSlice';
import { openAuthModal } from '@/store/slices/authSlice';
import { Show, Seat, Booking } from '@/types/models';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';

interface CheckoutModalProps {
  show: Show;
  selectedSeats: Seat[];
  onClose: () => void;
  onSuccess: (confirmedBooking: Booking) => void;
}

export default function CheckoutModal({ show, selectedSeats, onClose, onSuccess }: CheckoutModalProps) {
  const dispatch = useAppDispatch();
  const { currentUser, sagaStep } = useAppSelector((state) => state.booking);
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const [paymentMethod, setPaymentMethod] = useState<'UPI' | 'CARD' | 'NETBANKING'>('UPI');
  const [simulateFailure, setSimulateFailure] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const effectiveUserId = user ? String(user.id) : currentUser.id;
  const effectiveUserEmail = user ? user.email : currentUser.email;
  const effectiveUserName = user ? user.fullName : currentUser.name;
  const effectiveUserPhone = user?.phoneNumber || currentUser.phone;

  const basePrice = selectedSeats.reduce((sum, s) => sum + Number(s.price), 0);
  const convenienceFee = selectedSeats.length * 35.40;
  const gst = convenienceFee * 0.18;
  const grandTotal = (basePrice + convenienceFee + gst).toFixed(2);

  const handlePay = async () => {
    setErrorMessage(null);

    if (!isAuthenticated) {
      dispatch(openAuthModal('login'));
      return;
    }

    try {
      // SAGA Step 1: Lock seats in Redis
      dispatch(setSagaStep('LOCKING'));
      const lockRes = await bookingApi.lockSeats(show.id, effectiveUserId, selectedSeats.map(s => s.id));
      if (!lockRes.data || !lockRes.data.locked) {
        throw new Error(lockRes.message || "Failed to acquire distributed Redis seat lock.");
      }

      // SAGA Step 2: Initiate Booking
      dispatch(setSagaStep('INITIATING'));
      const bookingRes = await bookingApi.initiateBooking({
        userId: effectiveUserId,
        userEmail: effectiveUserEmail,
        userPhone: effectiveUserPhone,
        showId: show.id,
        movieTitle: show.movieTitle || "Dune: Part Two",
        theatreName: show.theatreName,
        screenName: show.screenName,
        showTime: show.startTime,
        seatIds: selectedSeats.map(s => s.id),
        totalAmount: grandTotal,
        paymentMethod
      });

      const booking = bookingRes.data;

      // SAGA Step 3: Process Payment
      dispatch(setSagaStep('PAYING'));
      const idempotencyKey = `idemp_${booking.bookingId}_${Date.now()}`;
      
      const paymentRes = await paymentApi.processPayment({
        bookingId: booking.bookingId,
        userId: currentUser.id,
        amount: grandTotal,
        paymentMethod,
        idempotencyKey,
        simulateFailure
      });

      // SAGA Step 4: Complete Booking
      dispatch(setSagaStep('CONFIRMING'));
      await bookingApi.confirmBooking(booking.bookingId, paymentRes.data.transactionId);

      const confirmedBooking: Booking = {
        ...booking,
        status: 'CONFIRMED',
        paymentTransactionId: paymentRes.data.transactionId,
        seatNumbers: selectedSeats.map(s => s.seatNumber),
        createdAt: new Date().toISOString()
      };

      dispatch(addConfirmedBooking(confirmedBooking));
      dispatch(setSagaStep('IDLE'));
      onSuccess(confirmedBooking);

    } catch (err: unknown) {
      console.error("Saga error:", err);
      dispatch(setSagaStep('COMPENSATING'));
      const message = err instanceof Error ? err.message : "Payment or Saga transaction failed. Rollback executed.";
      setErrorMessage(message);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="bg-[#0f172a] border border-slate-800 rounded-2xl max-w-lg w-full overflow-hidden shadow-2xl">
        
        {/* Header */}
        <div className="px-6 py-4 bg-slate-900 border-b border-slate-800 flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-white">Review & Checkout</h3>
            <p className="text-xs text-slate-400">Saga Distributed Transaction</p>
          </div>
          <button 
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-800 hover:bg-slate-700 flex items-center justify-center text-slate-400 hover:text-white"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-5">
          {/* User Account Status */}
          {isAuthenticated && user ? (
            <div className="bg-emerald-950/20 border border-emerald-800/40 rounded-xl px-3.5 py-2 flex items-center justify-between text-xs">
              <div className="flex items-center gap-2 text-emerald-300">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                <span>Booking as <strong className="text-white">{effectiveUserName}</strong> ({effectiveUserEmail})</span>
              </div>
            </div>
          ) : (
            <div className="bg-rose-950/20 border border-rose-800/40 rounded-xl px-3.5 py-2 flex items-center justify-between text-xs">
              <span className="text-slate-300">Have an account? Sign in to save digital tickets.</span>
              <button
                type="button"
                onClick={() => dispatch(openAuthModal('login'))}
                className="text-rose-400 hover:text-rose-300 font-semibold underline underline-offset-2 flex items-center gap-1"
              >
                <LogIn className="w-3.5 h-3.5" /> Sign In
              </button>
            </div>
          )}

          {/* Movie & Show Info */}
          <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3.5">
            <h4 className="text-sm font-semibold text-rose-400">{show.movieTitle || "Dune: Part Two"}</h4>
            <p className="text-xs text-slate-300 mt-0.5">{show.theatreName} • {show.screenName}</p>
            <div className="flex items-center justify-between mt-2 pt-2 border-t border-slate-800/50 text-xs text-slate-400">
              <span>Show Time: <strong className="text-white">{show.startTime}</strong></span>
              <span>Seats: <strong className="text-white">{selectedSeats.map(s => s.seatNumber).join(', ')}</strong></span>
            </div>
          </div>

          {/* Price Breakdown */}
          <div className="space-y-2 text-xs">
            <div className="flex justify-between text-slate-300">
              <span>Ticket Base Price ({selectedSeats.length} seats)</span>
              <span>₹{basePrice.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-slate-400">
              <span>Convenience Fee</span>
              <span>₹{convenienceFee.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-slate-400">
              <span>Integrated GST (18%)</span>
              <span>₹{gst.toFixed(2)}</span>
            </div>
            <Separator className="my-2" />
            <div className="flex justify-between text-sm font-bold text-white pt-1">
              <span>Grand Total</span>
              <span className="text-rose-400">₹{grandTotal}</span>
            </div>
          </div>

          {/* Payment Method Selector */}
          <div>
            <label className="text-xs font-semibold text-slate-300 block mb-2">Payment Method</label>
            <div className="grid grid-cols-3 gap-2">
              {(['UPI', 'CARD', 'NETBANKING'] as const).map((method) => (
                <button
                  key={method}
                  type="button"
                  onClick={() => setPaymentMethod(method)}
                  className={`py-2 px-3 rounded-xl border text-xs font-semibold flex items-center justify-center gap-1.5 transition-all ${
                    paymentMethod === method
                      ? 'bg-rose-600/10 border-rose-500 text-rose-400'
                      : 'bg-slate-900 border-slate-800 text-slate-400 hover:text-white'
                  }`}
                >
                  <CreditCard className="w-3.5 h-3.5" />
                  <span>{method}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Saga Simulation Switch */}
          <div className="p-3 bg-indigo-950/30 border border-indigo-900/50 rounded-xl">
            <label className="flex items-center gap-2 cursor-pointer text-xs">
              <input
                type="checkbox"
                checked={simulateFailure}
                onChange={(e) => setSimulateFailure(e.target.checked)}
                className="rounded border-slate-700 text-rose-600 focus:ring-0 cursor-pointer"
              />
              <span className="text-indigo-200 font-medium">
                Test Saga Rollback (Simulate Payment Failure)
              </span>
            </label>
            <p className="text-[11px] text-indigo-400/80 mt-1 pl-5">
              Simulates downstream payment decline to verify Saga compensating transactions (releasing seat locks & cancelling booking).
            </p>
          </div>

          {/* Saga Progress Status */}
          {sagaStep !== 'IDLE' && (
            <div className="p-3 bg-slate-900 border border-slate-800 rounded-xl text-xs space-y-1.5">
              <div className="flex items-center justify-between font-mono text-[11px]">
                <span className="text-slate-400">Saga Coordinator State:</span>
                <Badge variant={sagaStep === 'COMPENSATING' ? 'destructive' : 'amber'}>
                  {sagaStep}
                </Badge>
              </div>
              <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                <div 
                  className={`h-full transition-all duration-300 ${
                    sagaStep === 'COMPENSATING' ? 'bg-rose-500 w-full' : 'bg-emerald-500 w-3/4 animate-pulse'
                  }`}
                />
              </div>
            </div>
          )}

          {errorMessage && (
            <div className="p-3 bg-rose-950/60 border border-rose-500/40 rounded-xl text-xs text-rose-300 flex items-start gap-2">
              <RotateCcw className="w-4 h-4 text-rose-400 flex-shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Saga Rollback Triggered</p>
                <p className="text-[11px] text-rose-300/80 mt-0.5">{errorMessage}</p>
              </div>
            </div>
          )}

          {/* Pay Button */}
          <Button
            variant="gradient"
            size="lg"
            onClick={handlePay}
            disabled={sagaStep === 'LOCKING' || sagaStep === 'INITIATING' || sagaStep === 'PAYING'}
            className="w-full flex items-center justify-center gap-2"
          >
            <span>{isAuthenticated ? `Pay ₹${grandTotal}` : `Sign In & Pay ₹${grandTotal}`}</span>
            <ArrowRight className="w-4 h-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}

