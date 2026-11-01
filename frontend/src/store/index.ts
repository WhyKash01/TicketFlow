import { configureStore } from '@reduxjs/toolkit';
import cityReducer from './slices/citySlice';
import bookingReducer from './slices/bookingSlice';
import catalogReducer from './slices/catalogSlice';

export const store = configureStore({
  reducer: {
    city: cityReducer,
    booking: bookingReducer,
    catalog: catalogReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

