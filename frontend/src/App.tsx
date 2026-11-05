import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import HomePage from './components/HomePage';
import MovieDetailPage from './components/MovieDetailPage';
import SeatBookingPage from './components/SeatBookingPage';
import MyBookingsPage from './components/MyBookingsPage';
import AuthModal from './components/AuthModal';
import { useAppDispatch, useAppSelector } from './store/hooks';
import { setSelectedMovie, setSelectedShow } from './store/slices/bookingSlice';
import { updateUser, logout } from './store/slices/authSlice';
import { authApi } from './api/client';
import { Movie, Show } from './types/models';

type PageState = 'home' | 'movie-detail' | 'seat-booking' | 'my-bookings';

export default function App() {
  const dispatch = useAppDispatch();
  const { selectedMovie, selectedShow } = useAppSelector((state) => state.booking);
  const [currentPage, setCurrentPage] = useState<PageState>('home');

  useEffect(() => {
    const token = localStorage.getItem('ticketflow_access_token');
    if (token) {
      authApi.getCurrentUser()
        .then((user) => {
          if (user) {
            dispatch(updateUser(user));
          } else {
            dispatch(logout());
          }
        })
        .catch(() => {
          // Leave stored session or offline fallback
        });
    }
  }, [dispatch]);

  const handleSelectMovie = (movie: Movie) => {
    dispatch(setSelectedMovie(movie));
    setCurrentPage('movie-detail');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSelectShow = (show: Show) => {
    dispatch(setSelectedShow(show));
    setCurrentPage('seat-booking');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleNavigate = (page: PageState) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="min-h-screen bg-[#0b0f19] text-slate-100 flex flex-col font-sans selection:bg-rose-500 selection:text-white">
      
      {/* Navigation Bar */}
      <Navbar onNavigate={handleNavigate} currentPage={currentPage} />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-6 w-full">
        {currentPage === 'home' && (
          <HomePage 
            onSelectMovie={handleSelectMovie} 
            onSelectEvent={() => alert("Event pass booking opening soon! Try booking a movie ticket below.")}
          />
        )}

        {currentPage === 'movie-detail' && selectedMovie && (
          <MovieDetailPage 
            movie={selectedMovie} 
            onBack={() => setCurrentPage('home')}
            onSelectShow={handleSelectShow}
          />
        )}

        {currentPage === 'seat-booking' && selectedShow && (
          <SeatBookingPage 
            show={selectedShow}
            onBack={() => setCurrentPage('movie-detail')}
            onCompleteBooking={() => setCurrentPage('home')}
          />
        )}

        {currentPage === 'my-bookings' && (
          <MyBookingsPage onBackToHome={() => setCurrentPage('home')} />
        )}
      </main>

      {/* Footer */}
      <footer className="mt-auto border-t border-slate-900 bg-[#070a12] py-8 text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <span className="font-bold text-slate-400">TicketFlow Engine</span>
            <span>•</span>
            <span>React + TypeScript + Redux Toolkit + Tailwind CSS + shadcn/ui</span>
          </div>
          <p className="text-slate-600">
            Inspired by BookMyShow & District. Production-grade concurrency & distributed transaction patterns.
          </p>
        </div>
      </footer>

      {/* Global Auth Modal */}
      <AuthModal />

    </div>
  );
}

