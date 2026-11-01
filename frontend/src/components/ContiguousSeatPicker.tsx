import React, { useState } from 'react';
import { Cpu, Sparkles, CheckCircle2 } from 'lucide-react';
import { bookingApi } from '@/api/client';
import { Seat } from '@/types/models';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';

interface ContiguousSeatPickerProps {
  showId: number;
  onSeatsAllocated?: (seats: Seat[]) => void;
}

export default function ContiguousSeatPicker({ showId, onSeatsAllocated }: ContiguousSeatPickerProps) {
  const [seatCount, setSeatCount] = useState<number>(2);
  const [preferredTier, setPreferredTier] = useState<string>('GOLD');
  const [loading, setLoading] = useState<boolean>(false);
  const [resultMessage, setResultMessage] = useState<{ success: boolean; text: string } | null>(null);

  const handleAutoAllocate = async () => {
    setLoading(true);
    setResultMessage(null);
    try {
      const res = await bookingApi.autoAllocateContiguous(showId, seatCount, preferredTier);
      if (res.data && res.data.found) {
        setResultMessage({
          success: true,
          text: `Allocated ${seatCount} contiguous seats in Row ${res.data.rowName} via Segment Tree!`,
        });
        if (onSeatsAllocated) {
          onSeatsAllocated(res.data.allocatedSeats);
        }
      } else {
        setResultMessage({
          success: false,
          text: res.message || 'No contiguous seats available for the chosen criteria.'
        });
      }
    } catch {
      setResultMessage({
        success: false,
        text: 'Failed to run contiguous allocation algorithm.'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card className="my-4 max-w-2xl mx-auto border-slate-800 bg-slate-900/80 shadow-lg">
      <CardContent className="p-4">
        <div className="flex items-center justify-between gap-3 mb-3">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-indigo-500/10 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
              <Cpu className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-white flex items-center gap-1.5">
                Segment Tree Contiguous Allocation
                <Badge variant="indigo" className="font-mono text-[10px] px-1.5 py-0.5">
                  O(log N)
                </Badge>
              </h3>
              <p className="text-xs text-slate-400">
                Instantly find contiguous companion seats together without gaps
              </p>
            </div>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {/* Number of contiguous seats */}
          <div className="flex items-center gap-1 bg-slate-950 p-1 rounded-lg border border-slate-800">
            {[1, 2, 3, 4, 5].map((num) => (
              <button
                key={num}
                type="button"
                onClick={() => setSeatCount(num)}
                className={`w-7 h-7 rounded text-xs font-bold transition-all ${
                  seatCount === num
                    ? 'bg-indigo-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                {num}
              </button>
            ))}
          </div>

          {/* Tier dropdown */}
          <select
            value={preferredTier}
            onChange={(e) => setPreferredTier(e.target.value)}
            className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded-lg px-2.5 py-1.5 focus:outline-none focus:border-indigo-500 cursor-pointer"
          >
            <option value="PLATINUM">Platinum (₹450)</option>
            <option value="GOLD">Gold (₹320)</option>
            <option value="SILVER">Silver (₹220)</option>
          </select>

          {/* Allocate Button */}
          <Button
            variant="indigo"
            size="sm"
            onClick={handleAutoAllocate}
            disabled={loading}
            className="ml-auto flex items-center gap-1.5 text-xs font-semibold"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>{loading ? 'Computing...' : 'Find Contiguous Seats'}</span>
          </Button>
        </div>

        {resultMessage && (
          <div className={`mt-3 p-2.5 rounded-lg text-xs flex items-center gap-2 ${
            resultMessage.success 
              ? 'bg-emerald-950/60 border border-emerald-500/30 text-emerald-300' 
              : 'bg-rose-950/60 border border-rose-500/30 text-rose-300'
          }`}>
            {resultMessage.success && <CheckCircle2 className="w-4 h-4 flex-shrink-0" />}
            <span>{resultMessage.text}</span>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

