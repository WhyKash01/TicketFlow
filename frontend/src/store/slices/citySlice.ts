import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface CityState {
  selectedCity: string;
  availableCities: string[];
}

const initialState: CityState = {
  selectedCity: 'Mumbai',
  availableCities: ['Mumbai', 'Delhi-NCR', 'Bengaluru', 'Hyderabad', 'Pune', 'Chennai'],
};

export const citySlice = createSlice({
  name: 'city',
  initialState,
  reducers: {
    setSelectedCity: (state, action: PayloadAction<string>) => {
      state.selectedCity = action.payload;
    },
  },
});

export const { setSelectedCity } = citySlice.actions;
export default citySlice.reducer;

