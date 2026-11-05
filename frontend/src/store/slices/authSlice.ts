import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { User } from '@/types/models';

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  isAuthModalOpen: boolean;
  authModalTab: 'login' | 'register';
}

const loadStoredUser = (): User | null => {
  try {
    const raw = localStorage.getItem('ticketflow_user');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

const initialAccessToken = localStorage.getItem('ticketflow_access_token');
const initialRefreshToken = localStorage.getItem('ticketflow_refresh_token');
const storedUser = loadStoredUser();

const initialState: AuthState = {
  user: storedUser,
  accessToken: initialAccessToken,
  refreshToken: initialRefreshToken,
  isAuthenticated: !!initialAccessToken && !!storedUser,
  isLoading: false,
  error: null,
  isAuthModalOpen: false,
  authModalTab: 'login',
};

export const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (
      state,
      action: PayloadAction<{
        user: User;
        accessToken: string;
        refreshToken: string;
      }>
    ) => {
      state.user = action.payload.user;
      state.accessToken = action.payload.accessToken;
      state.refreshToken = action.payload.refreshToken;
      state.isAuthenticated = true;
      state.error = null;
      state.isAuthModalOpen = false;

      localStorage.setItem('ticketflow_user', JSON.stringify(action.payload.user));
      localStorage.setItem('ticketflow_access_token', action.payload.accessToken);
      localStorage.setItem('ticketflow_refresh_token', action.payload.refreshToken);
    },
    updateUser: (state, action: PayloadAction<User>) => {
      state.user = action.payload;
      localStorage.setItem('ticketflow_user', JSON.stringify(action.payload));
    },
    logout: (state) => {
      state.user = null;
      state.accessToken = null;
      state.refreshToken = null;
      state.isAuthenticated = false;
      state.error = null;

      localStorage.removeItem('ticketflow_user');
      localStorage.removeItem('ticketflow_access_token');
      localStorage.removeItem('ticketflow_refresh_token');
    },
    openAuthModal: (state, action: PayloadAction<'login' | 'register' | undefined>) => {
      state.authModalTab = action.payload || 'login';
      state.isAuthModalOpen = true;
      state.error = null;
    },
    closeAuthModal: (state) => {
      state.isAuthModalOpen = false;
      state.error = null;
    },
    setAuthModalTab: (state, action: PayloadAction<'login' | 'register'>) => {
      state.authModalTab = action.payload;
      state.error = null;
    },
    setAuthLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
    setAuthError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
      state.isLoading = false;
    },
  },
});

export const {
  setCredentials,
  updateUser,
  logout,
  openAuthModal,
  closeAuthModal,
  setAuthModalTab,
  setAuthLoading,
  setAuthError,
} = authSlice.actions;

export default authSlice.reducer;

