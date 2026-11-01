export type SeatTier = 'PLATINUM' | 'GOLD' | 'SILVER' | 'VIP';
export type SeatStatus = 'AVAILABLE' | 'LOCKED' | 'BOOKED' | 'BLOCKED';
export type BookingStatus = 'PENDING' | 'SEATS_LOCKED' | 'PAYMENT_PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED' | 'REFUNDED';
export type PaymentStatus = 'INITIATED' | 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';

export interface Movie {
  id: number;
  title: string;
  description: string;
  genre: string;
  language: string;
  durationMinutes: number;
  rating: number;
  certificate?: string;
  posterUrl: string;
  backdropUrl?: string;
  trailerUrl?: string;
  releaseDate?: string;
  isTrending?: boolean;
}

export interface EventItem {
  id: number;
  title: string;
  description: string;
  category: string;
  performer?: string;
  venue: string;
  city: string;
  bannerUrl: string;
  priceStartingFrom: number;
  isFeatured?: boolean;
}

export interface Theatre {
  id: number;
  name: string;
  brand?: string;
  city: string;
  location?: string;
  address?: string;
}

export interface Screen {
  id: number;
  name: string;
  screenFormat: string;
  totalRows: number;
  seatsPerRow: number;
  totalCapacity: number;
}

export interface Show {
  id: number;
  movieId?: number;
  eventId?: number;
  theatreId: number;
  screenId?: number;
  movieTitle: string;
  theatreName: string;
  screenName: string;
  city: string;
  showDate?: string;
  startTime: string;
  endTime?: string;
  soundExperience?: string;
  basePrice: number;
}

export interface Seat {
  id: number;
  showId: number;
  seatNumber: string;
  rowName: string;
  colIndex: number;
  tier: SeatTier;
  price: number;
  status: SeatStatus;
  lockedByUserId?: string | null;
  lockExpiresAtEpochMs?: number;
}

export interface Booking {
  bookingId: string;
  bookingReference: string;
  userId: string;
  userEmail?: string;
  userPhone?: string;
  showId: number;
  movieTitle: string;
  theatreName: string;
  screenName: string;
  showTime: string;
  seatIds?: number[];
  seatNumbers: string[];
  totalAmount: number | string;
  status: BookingStatus;
  paymentTransactionId?: string;
  qrCodePayload?: string;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
}

export interface SeatLockResponse {
  locked: boolean;
  lockId?: string;
  showId: number;
  lockedSeatIds: number[];
  failedSeatIds?: number[];
  expiresAtEpochMs?: number;
  message?: string;
}

export interface ContiguousSeatResponse {
  found: boolean;
  showId: number;
  rowName?: string;
  allocatedSeats: Seat[];
  algorithmUsed?: string;
  message?: string;
}

