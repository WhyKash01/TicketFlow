import { configureStore } from '@reduxjs/toolkit';
import cityReducer from './slices/citySlice';
import bookingReducer from './slices/bookingSlice';
import catalogReducer from './slices/catalogSlice';
import authReducer from './slices/authSlice';

export const store = configureStore({
  reducer: {
    city: cityReducer,
    booking: bookingReducer,
    catalog: catalogReducer,
    auth: authReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

