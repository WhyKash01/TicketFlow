import React, { useState, useEffect } from 'react';
import { ArrowLeft, Star, Clock, Calendar, MapPin, Play, Sparkles } from 'lucide-react';
import { catalogApi } from '@/api/client';
import { useAppSelector } from '@/store/hooks';
import { Movie, Show } from '@/types/models';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';

interface MovieDetailPageProps {
  movie: Movie;
  onBack: () => void;
  onSelectShow: (show: Show) => void;
}

export default function MovieDetailPage({ movie, onBack, onSelectShow }: MovieDetailPageProps) {
  const { selectedCity } = useAppSelector((state) => state.city);
  const [shows, setShows] = useState<Show[]>([]);
  const [selectedDateIndex, setSelectedDateIndex] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);

  // Generate 4 calendar days (Today, Tomorrow, +2, +3)
  const dates = Array.from({ length: 4 }).map((_, i) => {
    const d = new Date();
    d.setDate(d.getDate() + i);
    return {
      dayName: i === 0 ? 'Today' : (i === 1 ? 'Tomorrow' : d.toLocaleDateString('en-US', { weekday: 'short' })),
      dateStr: d.toLocaleDateString('en-US', { day: 'numeric', month: 'short' }),
      fullDate: d.toISOString().split('T')[0]
    };
  });

  useEffect(() => {
    async function loadShows() {
      setLoading(true);
      try {
        const fetchedShows = await catalogApi.getShowsForMovie(movie.id, selectedCity);
        setShows(fetchedShows || []);
      } catch (err) {
        console.error("Failed to load shows:", err);
      } finally {
        setLoading(false);
      }
    }
    loadShows();
  }, [movie.id, selectedCity]);

  // Group shows by Theatre
  const showsByTheatre = shows.reduce<Record<string, { theatreName: string; city: string; showList: Show[] }>>((acc, show) => {
    const key = show.theatreName;
    if (!acc[key]) acc[key] = { theatreName: show.theatreName, city: show.city, showList: [] };
    acc[key].showList.push(show);
    return acc;
  }, {});

  return (
    <div className="space-y-8 pb-16 animate-fade-in">
      
      {/* Back button */}
      <Button
        variant="ghost"
        size="sm"
        onClick={onBack}
        className="inline-flex items-center gap-2 text-xs font-semibold text-slate-400 hover:text-white"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>Back to Movies</span>
      </Button>

      {/* Movie Hero Banner */}
      <div className="relative rounded-3xl overflow-hidden border border-slate-800 bg-slate-900">
        <div className="absolute inset-0 z-0">
          <img 
            src={movie.backdropUrl || movie.posterUrl} 
            alt={movie.title}
            className="w-full h-full object-cover opacity-25 filter blur-sm scale-105"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-[#0b0f19] via-[#0b0f19]/70 to-transparent" />
        </div>

        <div className="relative z-10 p-6 sm:p-8 flex flex-col md:flex-row gap-6 sm:gap-8 items-start">
          {/* Poster */}
          <div className="w-40 sm:w-52 aspect-[2/3] rounded-2xl overflow-hidden border border-slate-700 shadow-2xl flex-shrink-0 bg-slate-950">
            <img src={movie.posterUrl} alt={movie.title} className="w-full h-full object-cover" />
          </div>

          {/* Details */}
          <div className="space-y-4 flex-1">
            <h1 className="text-2xl sm:text-4xl font-extrabold text-white tracking-tight">
              {movie.title}
            </h1>

            <div className="flex flex-wrap items-center gap-3 text-xs font-semibold text-slate-300">
              <Badge variant="amber" className="gap-1 py-1 px-2.5">
                <Star className="w-3.5 h-3.5 fill-current" />
                {movie.rating} / 10
              </Badge>
              <Badge variant="outline">
                {movie.certificate || 'U/A'}
              </Badge>
              <span>{movie.genre}</span>
              <span>•</span>
              <span className="flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-slate-400" />
                {movie.durationMinutes} mins
              </span>
              <span>•</span>
              <span>{movie.language}</span>
            </div>

            <p className="text-xs sm:text-sm text-slate-300 leading-relaxed max-w-3xl">
              {movie.description}
            </p>

            {movie.trailerUrl && (
              <a
                href={movie.trailerUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-800/90 hover:bg-slate-700 text-white text-xs font-semibold border border-slate-700 transition-colors"
              >
                <Play className="w-3.5 h-3.5 text-rose-500 fill-rose-500" />
                <span>Play Official Trailer</span>
              </a>
            )}
          </div>
        </div>
      </div>

      {/* Date Picker Ribbon */}
      <div className="flex items-center gap-3 overflow-x-auto pb-2 border-b border-slate-800">
        <span className="text-xs font-bold uppercase text-slate-400 mr-2 flex items-center gap-1">
          <Calendar className="w-3.5 h-3.5 text-rose-500" /> Select Date:
        </span>
        {dates.map((d, index) => (
          <button
            key={d.fullDate}
            type="button"
            onClick={() => setSelectedDateIndex(index)}
            className={`px-4 py-2 rounded-xl text-center transition-all ${
              selectedDateIndex === index
                ? 'bg-rose-600 text-white font-bold shadow-lg shadow-rose-600/30'
                : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-white'
            }`}
          >
            <span className="block text-[10px] uppercase">{d.dayName}</span>
            <span className="block text-xs font-bold mt-0.5">{d.dateStr}</span>
          </button>
        ))}
      </div>

      {/* Cinema / Theatres Showtimes */}
      <div className="space-y-4">
        <h2 className="text-lg font-bold text-white flex items-center gap-2">
          <span>Theatres & Showtimes in {selectedCity}</span>
          <span className="text-xs font-normal text-slate-400">({Object.keys(showsByTheatre).length} cinemas available)</span>
        </h2>

        {Object.keys(showsByTheatre).length === 0 ? (
          <div className="p-8 text-center bg-slate-900/40 border border-slate-800 rounded-2xl">
            <p className="text-sm text-slate-400">No shows currently listed for this date in {selectedCity}.</p>
          </div>
        ) : (
          Object.values(showsByTheatre).map((theatreGroup) => (
            <Card
              key={theatreGroup.theatreName}
              className="bg-slate-900/60 border-slate-800 p-5 space-y-4"
            >
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-sm sm:text-base font-bold text-white">
                    {theatreGroup.theatreName}
                  </h3>
                  <p className="text-xs text-slate-400 flex items-center gap-1 mt-0.5">
                    <MapPin className="w-3.5 h-3.5 text-slate-500" />
                    <span>{theatreGroup.city}</span>
                  </p>
                </div>
                <Badge variant="emerald" className="gap-1.5 py-1">
                  <Sparkles className="w-3 h-3" />
                  <span>Instant Redis Distributed Seat Locking</span>
                </Badge>
              </div>

              {/* Show Time Pills */}
              <div className="flex flex-wrap items-center gap-3 pt-2 border-t border-slate-800/80">
                {theatreGroup.showList.map((show) => (
                  <button
                    key={show.id}
                    type="button"
                    onClick={() => onSelectShow({ ...show, movieTitle: movie.title })}
                    className="group px-4 py-2.5 rounded-xl border border-slate-700 bg-slate-800/60 hover:bg-rose-600 hover:border-rose-500 text-left transition-all hover:scale-105"
                  >
                    <span className="block text-xs font-extrabold text-white group-hover:text-white">
                      {show.startTime}
                    </span>
                    <span className="block text-[10px] text-slate-400 group-hover:text-rose-100">
                      {show.screenName}
                    </span>
                    <span className="block text-[10px] text-rose-400 group-hover:text-white font-semibold">
                      ₹{show.basePrice}
                    </span>
                  </button>
                ))}
              </div>
            </Card>
          ))
        )}
      </div>

    </div>
  );
}

