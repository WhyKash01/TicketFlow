import React, { useState, useEffect } from 'react';
import { Clock, AlertTriangle } from 'lucide-react';

interface LockCountdownTimerProps {
  expiresAtEpochMs?: number | null;
  onExpire?: () => void;
}

export default function LockCountdownTimer({ expiresAtEpochMs, onExpire }: LockCountdownTimerProps) {
  const [timeLeftMs, setTimeLeftMs] = useState<number>(600000); // 10 minutes default

  useEffect(() => {
    const target = expiresAtEpochMs || (Date.now() + 600000);

    const interval = setInterval(() => {
      const remaining = target - Date.now();
      if (remaining <= 0) {
        clearInterval(interval);
        setTimeLeftMs(0);
        if (onExpire) onExpire();
      } else {
        setTimeLeftMs(remaining);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [expiresAtEpochMs, onExpire]);

  const minutes = Math.floor(timeLeftMs / 60000);
  const seconds = Math.floor((timeLeftMs % 60000) / 1000);
  const isCritical = minutes < 2;

  return (
    <div className={`inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full border text-xs font-mono font-semibold transition-all ${
      isCritical 
        ? 'bg-rose-950/80 border-rose-500 text-rose-300 animate-pulse' 
        : 'bg-amber-950/50 border-amber-600/40 text-amber-400'
    }`}>
      {isCritical ? <AlertTriangle className="w-3.5 h-3.5" /> : <Clock className="w-3.5 h-3.5" />}
      <span>Seat Lock Expires in:</span>
      <span className="text-sm font-bold">
        {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
      </span>
    </div>
  );
}

