import React from 'react';
import { Ticket, QrCode, ArrowLeft, Download } from 'lucide-react';
import { useAppSelector } from '@/store/hooks';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';

interface MyBookingsPageProps {
  onBackToHome: () => void;
}

export default function MyBookingsPage({ onBackToHome }: MyBookingsPageProps) {
  const { userBookings } = useAppSelector((state) => state.booking);

  return (
    <div className="space-y-6 max-w-4xl mx-auto pb-16 animate-fade-in">
      
      {/* Top Header */}
      <div className="flex items-center justify-between pb-4 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="icon"
            onClick={onBackToHome}
            className="rounded-full"
          >
            <ArrowLeft className="w-4 h-4" />
          </Button>
          <div>
            <h1 className="text-xl font-bold text-white">My Tickets & Bookings</h1>
            <p className="text-xs text-slate-400">View and print your digital passes</p>
          </div>
        </div>
      </div>

      {userBookings.length === 0 ? (
        <Card className="p-12 text-center bg-slate-900/40 border-slate-800 rounded-3xl space-y-3">
          <div className="w-12 h-12 rounded-full bg-slate-800 text-slate-500 flex items-center justify-center mx-auto">
            <Ticket className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-slate-300">No Tickets Yet</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            You haven't booked any movie or event tickets. Explore trending movies to get started!
          </p>
          <Button
            variant="default"
            size="sm"
            onClick={onBackToHome}
            className="mt-3 text-xs"
          >
            Browse Movies
          </Button>
        </Card>
      ) : (
        <div className="space-y-4">
          {userBookings.map((b) => (
            <Card
              key={b.bookingId}
              className="bg-slate-900/70 border-slate-800 hover:border-slate-700 p-5 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 transition-all"
            >
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <Badge variant="rose" className="font-mono text-xs font-bold">
                    {b.bookingReference}
                  </Badge>
                  <Badge variant="emerald" className="text-[10px] uppercase font-bold">
                    Confirmed
                  </Badge>
                </div>
                <h3 className="text-base font-bold text-white">{b.movieTitle || "Dune: Part Two"}</h3>
                <p className="text-xs text-slate-400">
                  {b.theatreName} • {b.screenName} • Today, {b.showTime}
                </p>
                <div className="flex items-center gap-4 text-xs text-slate-300 pt-1">
                  <span>Seats: <strong className="text-white">{Array.isArray(b.seatNumbers) ? b.seatNumbers.join(', ') : 'A1, A2'}</strong></span>
                  <span>•</span>
                  <span>Paid: <strong className="text-emerald-400">₹{b.totalAmount}</strong></span>
                </div>
              </div>

              <div className="flex items-center gap-3 w-full md:w-auto pt-3 md:pt-0 border-t md:border-t-0 border-slate-800">
                <div className="w-12 h-12 bg-white p-1 rounded-lg flex items-center justify-center">
                  <QrCode className="w-10 h-10 text-slate-950" />
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => window.print()}
                  className="flex items-center gap-1.5"
                >
                  <Download className="w-3.5 h-3.5" />
                  <span>Print Ticket</span>
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

    </div>
  );
}

