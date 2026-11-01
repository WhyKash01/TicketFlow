import React, { useState, useEffect } from 'react';
import { ArrowLeft, Ticket } from 'lucide-react';
import { bookingApi } from '@/api/client';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import {
  setShowSeats,
  toggleSeatSelection,
  setSelectedSeatIds,
  clearSeatSelection,
  setLockExpiresAt,
} from '@/store/slices/bookingSlice';
import { Show, Seat, Booking } from '@/types/models';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import SeatMap from './SeatMap';
import ContiguousSeatPicker from './ContiguousSeatPicker';
import LockCountdownTimer from './LockCountdownTimer';
import CheckoutModal from './CheckoutModal';
import TicketConfirmation from './TicketConfirmation';

interface SeatBookingPageProps {
  show: Show;
  onBack: () => void;
  onCompleteBooking: () => void;
}

export default function SeatBookingPage({ show, onBack, onCompleteBooking }: SeatBookingPageProps) {
  const dispatch = useAppDispatch();
  const { currentUser, showSeats, selectedSeatIds, lockExpiresAt } = useAppSelector((state) => state.booking);
  const [isCheckoutOpen, setIsCheckoutOpen] = useState<boolean>(false);
  const [confirmedBooking, setConfirmedBooking] = useState<Booking | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  // Load seats from backend
  useEffect(() => {
    async function loadSeats() {
      setLoading(true);
      try {
        const fetchedSeats = await bookingApi.getSeats(show.id, currentUser.id);
        dispatch(setShowSeats(fetchedSeats || []));
      } catch (err) {
        console.error("Failed to load show seats:", err);
      } finally {
        setLoading(false);
      }
    }
    loadSeats();
  }, [show.id, currentUser.id, dispatch]);

  const handleToggleSeat = (seat: Seat) => {
    if (seat.status !== 'AVAILABLE') return;
    dispatch(toggleSeatSelection(seat.id));
  };

  const handleContiguousAllocated = (allocatedSeats: Seat[]) => {
    const ids = allocatedSeats.map(s => s.id);
    dispatch(setSelectedSeatIds(ids));
  };

  const selectedSeatsList = showSeats.filter(s => selectedSeatIds.includes(s.id));
  const totalAmount = selectedSeatsList.reduce((sum, s) => sum + Number(s.price), 0);

  if (confirmedBooking) {
    return (
      <TicketConfirmation 
        booking={confirmedBooking} 
        onBackToHome={onCompleteBooking} 
      />
    );
  }

  return (
    <div className="space-y-6 pb-28 animate-fade-in">
      
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="icon"
            onClick={onBack}
            className="rounded-full"
          >
            <ArrowLeft className="w-4 h-4" />
          </Button>
          <div>
            <h1 className="text-lg sm:text-xl font-bold text-white">
              {show.movieTitle || "Dune: Part Two"}
            </h1>
            <p className="text-xs text-slate-400 mt-0.5">
              {show.theatreName} • {show.screenName} • Today, {show.startTime}
            </p>
          </div>
        </div>

        {/* Lock Timer */}
        <LockCountdownTimer 
          expiresAtEpochMs={lockExpiresAt}
          onExpire={() => {
            alert("Your seat lock has expired. Re-selecting seats.");
            dispatch(clearSeatSelection());
          }}
        />
      </div>

      {/* Segment Tree Contiguous Seat Helper Widget */}
      <ContiguousSeatPicker
        showId={show.id}
        onSeatsAllocated={handleContiguousAllocated}
      />

      {/* Main Seat Map */}
      <Card className="border-slate-800/80 bg-slate-900/50 rounded-3xl p-6 sm:p-10 shadow-2xl backdrop-blur">
        {loading ? (
          <div className="py-20 text-center text-slate-400 text-sm animate-pulse">
            Loading real-time cinema seating matrix & Redis locks...
          </div>
        ) : (
          <SeatMap
            seats={showSeats}
            selectedSeatIds={selectedSeatIds}
            onToggleSeat={handleToggleSeat}
          />
        )}
      </Card>

      {/* Sticky Bottom Summary Bar */}
      {selectedSeatIds.length > 0 && (
        <div className="fixed bottom-0 inset-x-0 z-40 bg-slate-950/95 backdrop-blur-md border-t border-slate-800 p-4 shadow-2xl animate-slide-up">
          <div className="max-w-5xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
            
            <div className="flex items-center gap-4 text-xs">
              <div className="w-10 h-10 rounded-xl bg-rose-600/10 border border-rose-500/20 flex items-center justify-center text-rose-500">
                <Ticket className="w-5 h-5" />
              </div>
              <div>
                <p className="text-slate-400">
                  Selected Seats: <strong className="text-white font-mono">{selectedSeatsList.map(s => s.seatNumber).join(', ')}</strong> ({selectedSeatIds.length})
                </p>
                <p className="text-xs text-slate-400 mt-0.5">
                  Subtotal: <strong className="text-rose-400 text-sm">₹{totalAmount.toFixed(2)}</strong> (excl. convenience fee)
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3 w-full sm:w-auto">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => dispatch(clearSeatSelection())}
                className="text-xs font-semibold text-slate-400 hover:text-white"
              >
                Clear
              </Button>

              <Button
                variant="gradient"
                size="default"
                onClick={() => setIsCheckoutOpen(true)}
                className="flex-1 sm:flex-initial"
              >
                Proceed to Pay ₹{totalAmount.toFixed(2)}
              </Button>
            </div>

          </div>
        </div>
      )}

      {/* Saga Checkout Modal */}
      {isCheckoutOpen && (
        <CheckoutModal
          show={show}
          selectedSeats={selectedSeatsList}
          onClose={() => setIsCheckoutOpen(false)}
          onSuccess={(confirmed) => {
            setIsCheckoutOpen(false);
            setConfirmedBooking(confirmed);
          }}
        />
      )}

    </div>
  );
}

