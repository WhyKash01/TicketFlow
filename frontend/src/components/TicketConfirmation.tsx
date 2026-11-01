import React, { useEffect } from 'react';
import { CheckCircle2, Ticket, Download, ArrowLeft, QrCode } from 'lucide-react';
import confetti from 'canvas-confetti';
import { Booking } from '@/types/models';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';

interface TicketConfirmationProps {
  booking: Booking;
  onBackToHome: () => void;
}

export default function TicketConfirmation({ booking, onBackToHome }: TicketConfirmationProps) {
  useEffect(() => {
    try {
      confetti({
        particleCount: 80,
        spread: 70,
        origin: { y: 0.6 }
      });
    } catch {
      // fallback
    }
  }, []);

  return (
    <div className="max-w-2xl mx-auto py-10 px-4 animate-fade-in">
      
      {/* Top Banner */}
      <div className="text-center mb-8">
        <div className="inline-flex items-center justify-center w-14 h-14 bg-emerald-500/10 border border-emerald-500/30 rounded-full mb-3 text-emerald-400">
          <CheckCircle2 className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-extrabold text-white">Booking Confirmed!</h2>
        <p className="text-sm text-slate-400 mt-1">
          Your e-tickets have been issued and sent via Notification Service (RabbitMQ).
        </p>
      </div>

      {/* Ticket Card */}
      <Card className="relative bg-[#0f172a] border-slate-700 rounded-3xl overflow-hidden shadow-2xl">
        
        {/* Top Header */}
        <div className="bg-gradient-to-r from-rose-600 to-rose-700 px-6 py-4 flex items-center justify-between text-white">
          <div className="flex items-center gap-2">
            <Ticket className="w-5 h-5 transform -rotate-12" />
            <span className="font-extrabold text-base tracking-tight">TicketFlow Pass</span>
          </div>
          <span className="font-mono text-xs font-bold uppercase tracking-wider bg-black/20 px-2.5 py-1 rounded-full border border-white/20">
            {booking.bookingReference || "TF-2026-X89K"}
          </span>
        </div>

        {/* Ticket Details */}
        <CardContent className="p-6 space-y-6">
          <div>
            <Badge variant="rose" className="text-[10px] font-bold uppercase tracking-wider mb-1">
              Movie
            </Badge>
            <h3 className="text-xl font-bold text-white">{booking.movieTitle || "Dune: Part Two"}</h3>
            <p className="text-xs text-slate-400 mt-0.5">{booking.theatreName} • {booking.screenName}</p>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 p-4 bg-slate-950/70 border border-slate-800/80 rounded-2xl text-xs">
            <div>
              <span className="text-slate-500 block text-[10px] uppercase font-bold">Show Date</span>
              <strong className="text-white text-xs mt-0.5 block">Today</strong>
            </div>
            <div>
              <span className="text-slate-500 block text-[10px] uppercase font-bold">Show Time</span>
              <strong className="text-white text-xs mt-0.5 block">{booking.showTime || "19:00"}</strong>
            </div>
            <div>
              <span className="text-slate-500 block text-[10px] uppercase font-bold">Seats</span>
              <strong className="text-rose-400 text-xs mt-0.5 block">
                {Array.isArray(booking.seatNumbers) ? booking.seatNumbers.join(', ') : 'A1, A2'}
              </strong>
            </div>
            <div>
              <span className="text-slate-500 block text-[10px] uppercase font-bold">Total Paid</span>
              <strong className="text-emerald-400 text-xs mt-0.5 block">₹{booking.totalAmount}</strong>
            </div>
          </div>

          {/* Perforated Divider */}
          <div className="relative flex items-center justify-center my-4">
            <div className="border-t-2 border-dashed border-slate-800 w-full" />
            <div className="absolute -left-10 w-8 h-8 rounded-full bg-[#0b0f19] border-r border-slate-700" />
            <div className="absolute -right-10 w-8 h-8 rounded-full bg-[#0b0f19] border-l border-slate-700" />
          </div>

          {/* QR Code and Verification Token */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-slate-900/60 p-4 rounded-2xl border border-slate-800/60">
            <div className="flex items-center gap-3">
              <div className="w-16 h-16 bg-white p-1 rounded-xl flex items-center justify-center shadow">
                <QrCode className="w-14 h-14 text-slate-950" />
              </div>
              <div>
                <span className="text-[10px] uppercase font-bold tracking-wider text-slate-400">Digital Gate Pass</span>
                <p className="text-xs font-mono text-slate-300 break-all max-w-[240px]">
                  {booking.bookingReference}-VERIFIED
                </p>
                <span className="inline-block mt-1 text-[10px] text-emerald-400 font-medium">
                  • Ready for Scan at Turnstile
                </span>
              </div>
            </div>

            <Button 
              variant="outline"
              size="sm"
              onClick={() => window.print()}
              className="flex items-center gap-1.5"
            >
              <Download className="w-3.5 h-3.5" />
              <span>Save Ticket</span>
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Back button */}
      <div className="text-center mt-6">
        <Button
          variant="ghost"
          size="sm"
          onClick={onBackToHome}
          className="inline-flex items-center gap-2 text-xs font-semibold text-rose-400 hover:text-rose-300"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Movie Listings</span>
        </Button>
      </div>
    </div>
  );
}

