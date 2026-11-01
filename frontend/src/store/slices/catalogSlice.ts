import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Movie, EventItem } from '@/types/models';

interface CatalogState {
  movies: Movie[];
  events: EventItem[];
  selectedGenre: string;
  loading: boolean;
  error: string | null;
}

const initialState: CatalogState = {
  movies: [],
  events: [],
  selectedGenre: 'ALL',
  loading: false,
  error: null,
};

export const catalogSlice = createSlice({
  name: 'catalog',
  initialState,
  reducers: {
    setMovies: (state, action: PayloadAction<Movie[]>) => {
      state.movies = action.payload;
    },
    setEvents: (state, action: PayloadAction<EventItem[]>) => {
      state.events = action.payload;
    },
    setSelectedGenre: (state, action: PayloadAction<string>) => {
      state.selectedGenre = action.payload;
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload;
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
  },
});

export const {
  setMovies,
  setEvents,
  setSelectedGenre,
  setLoading,
  setError,
} = catalogSlice.actions;

export default catalogSlice.reducer;

