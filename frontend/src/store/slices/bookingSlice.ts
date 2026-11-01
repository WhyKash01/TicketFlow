import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Booking, Movie, Show, Seat } from '@/types/models';

export type SagaStep = 'IDLE' | 'LOCKING' | 'INITIATING' | 'PAYING' | 'CONFIRMING' | 'COMPENSATING';

interface UserProfile {
  id: string;
  name: string;
  email: string;
  phone: string;
}

interface BookingState {
  currentUser: UserProfile;
  selectedMovie: Movie | null;
  selectedShow: Show | null;
  showSeats: Seat[];
  selectedSeatIds: number[];
  lockExpiresAt: number | null;
  userBookings: Booking[];
  sagaStep: SagaStep;
}

const loadPersistedBookings = (): Booking[] => {
  try {
    const saved = localStorage.getItem('ticketflow_user_bookings');
    return saved ? JSON.parse(saved) : [];
  } catch {
    return [];
  }
};

const initialState: BookingState = {
  currentUser: {
    id: 'usr_' + Math.random().toString(36).substring(2, 8),
    name: 'Yash Sharma',
    email: 'yash@ticketflow.dev',
    phone: '+91 9876543210',
  },
  selectedMovie: null,
  selectedShow: null,
  showSeats: [],
  selectedSeatIds: [],
  lockExpiresAt: null,
  userBookings: loadPersistedBookings(),
  sagaStep: 'IDLE',
};

export const bookingSlice = createSlice({
  name: 'booking',
  initialState,
  reducers: {
    setSelectedMovie: (state, action: PayloadAction<Movie | null>) => {
      state.selectedMovie = action.payload;
    },
    setSelectedShow: (state, action: PayloadAction<Show | null>) => {
      state.selectedShow = action.payload;
      state.selectedSeatIds = [];
      state.lockExpiresAt = null;
    },
    setShowSeats: (state, action: PayloadAction<Seat[]>) => {
      state.showSeats = action.payload;
    },
    toggleSeatSelection: (state, action: PayloadAction<number>) => {
      const seatId = action.payload;
      if (state.selectedSeatIds.includes(seatId)) {
        state.selectedSeatIds = state.selectedSeatIds.filter(id => id !== seatId);
      } else {
        if (state.selectedSeatIds.length < 8) {
          state.selectedSeatIds.push(seatId);
        }
      }
    },
    setSelectedSeatIds: (state, action: PayloadAction<number[]>) => {
      state.selectedSeatIds = action.payload;
    },
    clearSeatSelection: (state) => {
      state.selectedSeatIds = [];
    },
    setLockExpiresAt: (state, action: PayloadAction<number | null>) => {
      state.lockExpiresAt = action.payload;
    },
    setSagaStep: (state, action: PayloadAction<SagaStep>) => {
      state.sagaStep = action.payload;
    },
    addConfirmedBooking: (state, action: PayloadAction<Booking>) => {
      state.userBookings.unshift(action.payload);
      try {
        localStorage.setItem('ticketflow_user_bookings', JSON.stringify(state.userBookings));
      } catch (e) {
        console.error('Failed to persist booking', e);
      }
    },
  },
});

export const {
  setSelectedMovie,
  setSelectedShow,
  setShowSeats,
  toggleSeatSelection,
  setSelectedSeatIds,
  clearSeatSelection,
  setLockExpiresAt,
  setSagaStep,
  addConfirmedBooking,
} = bookingSlice.actions;

export default bookingSlice.reducer;

