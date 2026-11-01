import React, { useState, useEffect } from 'react';
import { Play, Star, ChevronRight, Sparkles, Layers, ShieldCheck, Cpu, MapPin } from 'lucide-react';
import { catalogApi } from '@/api/client';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { setMovies, setEvents, setSelectedGenre, setLoading } from '@/store/slices/catalogSlice';
import { Movie, EventItem } from '@/types/models';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';

interface HomePageProps {
  onSelectMovie: (movie: Movie) => void;
  onSelectEvent?: (event: EventItem) => void;
}

export default function HomePage({ onSelectMovie, onSelectEvent }: HomePageProps) {
  const dispatch = useAppDispatch();
  const { selectedCity } = useAppSelector((state) => state.city);
  const { movies, events, selectedGenre, loading } = useAppSelector((state) => state.catalog);

  useEffect(() => {
    async function loadData() {
      dispatch(setLoading(true));
      try {
        const [movieList, eventList] = await Promise.all([
          catalogApi.getMovies(),
          catalogApi.getEvents()
        ]);
        dispatch(setMovies(movieList || []));
        dispatch(setEvents(eventList || []));
      } catch (err) {
        console.error("Failed to load catalog data:", err);
      } finally {
        dispatch(setLoading(false));
      }
    }
    loadData();
  }, [selectedCity, dispatch]);

  const genres = ['ALL', 'Sci-Fi / Adventure', 'Biography / Drama', 'Action'];
  const filteredMovies = selectedGenre === 'ALL' 
    ? movies 
    : movies.filter(m => m.genre?.toLowerCase().includes(selectedGenre.toLowerCase()));

  const featuredMovie = movies[0];

  return (
    <div className="space-y-12 pb-16">
      
      {/* Hero Banner */}
      {featuredMovie && (
        <div className="relative rounded-3xl overflow-hidden border border-slate-800 bg-slate-900 shadow-2xl">
          <div className="absolute inset-0 z-0">
            <img 
              src={featuredMovie.backdropUrl || featuredMovie.posterUrl} 
              alt={featuredMovie.title}
              className="w-full h-full object-cover opacity-35 filter brightness-75 scale-105"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-[#0b0f19] via-[#0b0f19]/60 to-transparent" />
            <div className="absolute inset-0 bg-gradient-to-r from-[#0b0f19] via-[#0b0f19]/40 to-transparent" />
          </div>

          <div className="relative z-10 p-6 sm:p-10 lg:p-12 max-w-2xl space-y-4">
            <Badge variant="rose" className="px-3 py-1 gap-1.5 font-semibold">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Trending in {selectedCity}</span>
            </Badge>

            <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight text-white leading-tight">
              {featuredMovie.title}
            </h1>

            <div className="flex items-center gap-4 text-xs font-semibold text-slate-300">
              <Badge variant="amber" className="gap-1 py-1 px-2.5">
                <Star className="w-3.5 h-3.5 fill-current" />
                {featuredMovie.rating} / 10
              </Badge>
              <span>{featuredMovie.certificate || 'U/A'}</span>
              <span>•</span>
              <span>{featuredMovie.genre}</span>
              <span>•</span>
              <span>{featuredMovie.durationMinutes} mins</span>
            </div>

            <p className="text-xs sm:text-sm text-slate-300 line-clamp-3 leading-relaxed">
              {featuredMovie.description}
            </p>

            <div className="flex flex-wrap items-center gap-3 pt-2">
              <Button
                variant="gradient"
                size="default"
                onClick={() => onSelectMovie(featuredMovie)}
                className="flex items-center gap-2"
              >
                <span>Book Tickets</span>
                <ChevronRight className="w-4 h-4" />
              </Button>

              {featuredMovie.trailerUrl && (
                <a
                  href={featuredMovie.trailerUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="px-4 py-2 bg-slate-800/80 hover:bg-slate-700 text-slate-200 font-semibold text-xs rounded-xl flex items-center gap-2 transition-colors border border-slate-700"
                >
                  <Play className="w-3.5 h-3.5 text-rose-500 fill-rose-500" />
                  <span>Watch Trailer</span>
                </a>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Engineering Architecture Showcase Strip */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 p-4 rounded-2xl bg-slate-900/40 border border-slate-800/80 text-xs">
        <div className="flex items-center gap-3 p-2">
          <div className="w-8 h-8 rounded-lg bg-rose-500/10 text-rose-400 flex items-center justify-center border border-rose-500/20">
            <ShieldCheck className="w-4 h-4" />
          </div>
          <div>
            <h4 className="font-bold text-slate-200">Redis Lock</h4>
            <p className="text-[11px] text-slate-400">Atomic 10m TTL lock</p>
          </div>
        </div>

        <div className="flex items-center gap-3 p-2">
          <div className="w-8 h-8 rounded-lg bg-indigo-500/10 text-indigo-400 flex items-center justify-center border border-indigo-500/20">
            <Cpu className="w-4 h-4" />
          </div>
          <div>
            <h4 className="font-bold text-slate-200">Segment Tree</h4>
            <p className="text-[11px] text-slate-400">O(log N) contiguous seats</p>
          </div>
        </div>

        <div className="flex items-center gap-3 p-2">
          <div className="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-400 flex items-center justify-center border border-amber-500/20">
            <Layers className="w-4 h-4" />
          </div>
          <div>
            <h4 className="font-bold text-slate-200">Saga Pattern</h4>
            <p className="text-[11px] text-slate-400">Distributed rollback</p>
          </div>
        </div>

        <div className="flex items-center gap-3 p-2">
          <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center border border-emerald-500/20">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <h4 className="font-bold text-slate-200">RabbitMQ</h4>
            <p className="text-[11px] text-slate-400">Async notifications</p>
          </div>
        </div>
      </div>

      {/* Movies in Theatres Section */}
      <section className="space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight">Now Showing</h2>
            <p className="text-xs text-slate-400 mt-0.5">Explore movies playing in cinemas across {selectedCity}</p>
          </div>

          {/* Genre Filters */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1">
            {genres.map(g => (
              <Button
                key={g}
                variant={selectedGenre === g ? 'default' : 'outline'}
                size="sm"
                onClick={() => dispatch(setSelectedGenre(g))}
                className="text-xs whitespace-nowrap"
              >
                {g}
              </Button>
            ))}
          </div>
        </div>

        {/* Movie Cards Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-5">
          {filteredMovies.map(movie => (
            <Card
              key={movie.id}
              onClick={() => onSelectMovie(movie)}
              className="group bg-slate-900/60 border-slate-800 hover:border-rose-500/50 overflow-hidden cursor-pointer transition-all hover:scale-[1.02] hover:shadow-xl hover:shadow-rose-950/20 flex flex-col"
            >
              <div className="relative aspect-[2/3] w-full overflow-hidden bg-slate-950">
                <img
                  src={movie.posterUrl}
                  alt={movie.title}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                />
                <div className="absolute top-2.5 right-2.5 bg-black/70 backdrop-blur-md px-2 py-1 rounded-md text-[11px] font-bold text-amber-400 flex items-center gap-1 border border-amber-400/20">
                  <Star className="w-3 h-3 fill-current" />
                  <span>{movie.rating}</span>
                </div>
              </div>

              <CardContent className="p-3.5 flex flex-col flex-1 justify-between">
                <div>
                  <h3 className="text-sm font-bold text-white group-hover:text-rose-400 transition-colors line-clamp-1">
                    {movie.title}
                  </h3>
                  <p className="text-[11px] text-slate-400 mt-1 line-clamp-1">{movie.genre}</p>
                </div>
                <div className="mt-3 pt-2.5 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-400">
                  <span>{movie.language?.split(',')[0]}</span>
                  <span className="text-rose-400 font-semibold group-hover:underline">Book Seats</span>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Live Events Section */}
      <section className="space-y-6 pt-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight">The Best Live Events</h2>
          <p className="text-xs text-slate-400 mt-0.5">Concerts, Standup Comedy, Performances</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {events.map(event => (
            <Card
              key={event.id}
              onClick={() => onSelectEvent && onSelectEvent(event)}
              className="group bg-slate-900/60 border-slate-800 hover:border-amber-500/50 overflow-hidden cursor-pointer transition-all hover:scale-[1.01] flex flex-col sm:flex-row"
            >
              <div className="sm:w-2/5 aspect-[16/9] sm:aspect-auto overflow-hidden bg-slate-950">
                <img
                  src={event.bannerUrl}
                  alt={event.title}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                />
              </div>

              <div className="p-5 sm:w-3/5 flex flex-col justify-between">
                <div>
                  <Badge variant="amber" className="text-[10px] uppercase font-bold tracking-wider">
                    {event.category}
                  </Badge>
                  <h3 className="text-base font-bold text-white mt-2 group-hover:text-amber-400 transition-colors">
                    {event.title}
                  </h3>
                  <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
                    <MapPin className="w-3.5 h-3.5 text-slate-500" />
                    <span>{event.venue}, {event.city}</span>
                  </p>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-800 flex items-center justify-between text-xs">
                  <span className="text-slate-400">Starting from <strong className="text-white">₹{event.priceStartingFrom}</strong></span>
                  <Button variant="default" size="sm" className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold">
                    Get Passes
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </section>

    </div>
  );
}

