import React from 'react';
import { Lock, Check } from 'lucide-react';
import { Seat } from '@/types/models';

interface SeatMapProps {
  seats: Seat[];
  selectedSeatIds: number[];
  onToggleSeat: (seat: Seat) => void;
}

export default function SeatMap({ seats, selectedSeatIds, onToggleSeat }: SeatMapProps) {
  // Group seats by row
  const rowsMap = seats.reduce<Record<string, Seat[]>>((acc, seat) => {
    if (!acc[seat.rowName]) acc[seat.rowName] = [];
    acc[seat.rowName].push(seat);
    return acc;
  }, {});

  const rowKeys = Object.keys(rowsMap).sort();

  return (
    <div className="w-full flex flex-col items-center">
      
      {/* Cinema Screen with curve & ambient lighting */}
      <div className="w-full max-w-2xl mb-10 flex flex-col items-center">
        <div className="cinema-screen w-full h-10 mb-2"></div>
        <p className="text-[11px] font-medium tracking-widest uppercase text-slate-400">
          All eyes this way please • Screen
        </p>
      </div>

      {/* Seat Matrix */}
      <div className="w-full max-w-3xl overflow-x-auto pb-6">
        <div className="min-w-[640px] flex flex-col gap-2.5 items-center">
          {rowKeys.map((rowName) => {
            const rowSeats = rowsMap[rowName].sort((a, b) => a.colIndex - b.colIndex);
            const tier = rowSeats[0]?.tier || 'SILVER';

            return (
              <div key={rowName} className="flex items-center gap-2">
                {/* Row Letter Left */}
                <span className="w-6 text-center text-xs font-bold text-slate-400">
                  {rowName}
                </span>

                {/* Seat buttons */}
                <div className="flex items-center gap-1.5 sm:gap-2">
                  {rowSeats.map((seat, idx) => {
                    const isSelected = selectedSeatIds.includes(seat.id);
                    const isBooked = seat.status === 'BOOKED';
                    const isLocked = seat.status === 'LOCKED';

                    // Insert middle aisle after 6th seat
                    const isAisle = idx === 5;

                    let seatStyle = 'bg-slate-800 text-slate-300 border-slate-700 hover:border-rose-400 hover:bg-slate-700';

                    if (isSelected) {
                      seatStyle = 'bg-rose-600 text-white border-rose-500 shadow-md shadow-rose-600/40 scale-105';
                    } else if (isBooked) {
                      seatStyle = 'bg-slate-900/60 text-slate-600 border-slate-900 cursor-not-allowed opacity-40';
                    } else if (isLocked) {
                      seatStyle = 'bg-amber-950/80 text-amber-300 border-amber-600/60 animate-seat-lock cursor-not-allowed';
                    }

                    return (
                      <React.Fragment key={seat.id}>
                        <button
                          type="button"
                          disabled={isBooked || isLocked}
                          onClick={() => onToggleSeat(seat)}
                          title={`${seat.seatNumber} (${tier} - ₹${seat.price}) - ${seat.status}`}
                          className={`w-7 h-7 sm:w-8 sm:h-8 rounded-t-lg rounded-b-sm border text-[11px] font-semibold flex items-center justify-center transition-all ${seatStyle}`}
                        >
                          {isSelected ? (
                            <Check className="w-3.5 h-3.5" />
                          ) : isLocked ? (
                            <Lock className="w-3 h-3 text-amber-400" />
                          ) : (
                            seat.colIndex
                          )}
                        </button>
                        {isAisle && <div className="w-6" />}
                      </React.Fragment>
                    );
                  })}
                </div>

                {/* Row Letter Right */}
                <span className="w-6 text-center text-xs font-bold text-slate-400">
                  {rowName}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Seat Status Legend */}
      <div className="flex flex-wrap items-center justify-center gap-6 mt-6 pt-4 border-t border-slate-800 text-xs text-slate-300">
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-t-sm bg-slate-800 border border-slate-700" />
          <span>Available</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-t-sm bg-rose-600 border border-rose-500 shadow-sm" />
          <span>Selected</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-t-sm bg-amber-950/80 border border-amber-500/80 flex items-center justify-center">
            <Lock className="w-2.5 h-2.5 text-amber-400" />
          </div>
          <span className="text-amber-300">Locked (Redis TTL)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-t-sm bg-slate-900 border border-slate-800 opacity-50" />
          <span className="text-slate-500">Reserved / Sold</span>
        </div>
      </div>

      {/* Tier Price Legend */}
      <div className="flex items-center justify-center gap-6 mt-3 text-[11px] font-medium text-slate-400">
        <span>Rows A-B: <strong className="text-rose-400">Platinum ₹450</strong></span>
        <span>•</span>
        <span>Rows C-E: <strong className="text-amber-400">Gold ₹320</strong></span>
        <span>•</span>
        <span>Rows F-H: <strong className="text-slate-300">Silver ₹220</strong></span>
      </div>
    </div>
  );
}

