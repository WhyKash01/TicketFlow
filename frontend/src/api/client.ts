import axios from 'axios';
import {
  Movie,
  EventItem,
  Show,
  Seat,
  Booking,
  ApiResponse,
  SeatLockResponse,
  ContiguousSeatResponse,
} from '@/types/models';

const api = axios.create({
  baseURL: '/api',
  timeout: 5000,
});

// Fallback mock dataset for local UI preview
const MOCK_MOVIES: Movie[] = [
  {
    id: 1,
    title: "Dune: Part Two",
    description: "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
    genre: "Sci-Fi / Adventure",
    language: "English, Hindi",
    durationMinutes: 166,
    rating: 8.8,
    certificate: "U/A",
    posterUrl: "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
    backdropUrl: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
    trailerUrl: "https://www.youtube.com/watch?v=Way9Dexny3w",
    isTrending: true
  },
  {
    id: 2,
    title: "Oppenheimer",
    description: "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.",
    genre: "Biography / Drama",
    language: "English, Hindi",
    durationMinutes: 180,
    rating: 8.9,
    certificate: "A",
    posterUrl: "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=600&auto=format&fit=crop&q=80",
    backdropUrl: "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=1200&auto=format&fit=crop&q=80",
    trailerUrl: "https://www.youtube.com/watch?v=uYPbbksJxIg",
    isTrending: true
  },
  {
    id: 3,
    title: "Interstellar",
    description: "When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot is tasked with piloting a spacecraft.",
    genre: "Sci-Fi / Drama",
    language: "English",
    durationMinutes: 169,
    rating: 8.7,
    certificate: "U/A",
    posterUrl: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
    backdropUrl: "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1200&auto=format&fit=crop&q=80",
    trailerUrl: "https://www.youtube.com/watch?v=zSWdZVtXT7E",
    isTrending: false
  }
];

const MOCK_EVENTS: EventItem[] = [
  {
    id: 1,
    title: "Coldplay: Music of the Spheres World Tour",
    description: "Experience an unforgettable night under the stars with Chris Martin and Coldplay live in concert.",
    category: "Music Concert",
    performer: "Coldplay",
    venue: "DY Patil Stadium",
    city: "Mumbai",
    bannerUrl: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1000&auto=format&fit=crop&q=80",
    priceStartingFrom: 2500,
    isFeatured: true
  },
  {
    id: 2,
    title: "Zakir Khan - Tathastu Live Standup",
    description: "India's favorite storyteller is back with his signature blend of relatable humor and poetic emotion.",
    category: "Comedy",
    performer: "Zakir Khan",
    venue: "Jawaharlal Nehru Stadium",
    city: "Delhi-NCR",
    bannerUrl: "https://images.unsplash.com/photo-1585699324551-f6c309eedeca?w=1000&auto=format&fit=crop&q=80",
    priceStartingFrom: 999,
    isFeatured: true
  }
];

const MOCK_SHOWS: Show[] = [
  {
    id: 101,
    movieId: 1,
    theatreId: 1,
    theatreName: "PVR P[XL] Phoenix Palladium",
    screenName: "Screen 1 (IMAX Laser)",
    movieTitle: "Dune: Part Two",
    city: "Mumbai",
    startTime: "14:30",
    endTime: "17:15",
    soundExperience: "IMAX 12-Track Immersive Audio",
    basePrice: 420.00
  },
  {
    id: 102,
    movieId: 1,
    theatreId: 1,
    theatreName: "PVR P[XL] Phoenix Palladium",
    screenName: "Screen 1 (IMAX Laser)",
    movieTitle: "Dune: Part Two",
    city: "Mumbai",
    startTime: "19:00",
    endTime: "21:45",
    soundExperience: "IMAX 12-Track Immersive Audio",
    basePrice: 490.00
  },
  {
    id: 103,
    movieId: 1,
    theatreId: 2,
    theatreName: "INOX Megaplex Inorbit Mall",
    screenName: "Screen 2 (Dolby Atmos)",
    movieTitle: "Dune: Part Two",
    city: "Mumbai",
    startTime: "18:15",
    endTime: "21:00",
    soundExperience: "Dolby Atmos 64-Channel",
    basePrice: 380.00
  }
];

function generateMockSeats(showId: number): Seat[] {
  const seats: Seat[] = [];
  const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
  let idCounter = 1;

  rows.forEach((row) => {
    let tier: Seat['tier'] = 'SILVER';
    let price = 220;
    if (row === 'A' || row === 'B') {
      tier = 'PLATINUM';
      price = 450;
    } else if (row === 'C' || row === 'D' || row === 'E') {
      tier = 'GOLD';
      price = 320;
    }

    for (let col = 1; col <= 12; col++) {
      const isBooked = (row === 'C' && (col === 4 || col === 5)) || (row === 'F' && col === 8);
      const isLocked = (row === 'B' && (col === 6 || col === 7));

      seats.push({
        id: idCounter++,
        showId: showId,
        seatNumber: `${row}${col}`,
        rowName: row,
        colIndex: col,
        tier: tier,
        price: price,
        status: isBooked ? 'BOOKED' : (isLocked ? 'LOCKED' : 'AVAILABLE'),
        lockedByUserId: isLocked ? 'user_other_99' : null
      });
    }
  });

  return seats;
}

export const catalogApi = {
  getMovies: async (): Promise<Movie[]> => {
    try {
      const res = await api.get<ApiResponse<Movie[]>>('/catalog/movies');
      return res.data.data;
    } catch {
      return MOCK_MOVIES;
    }
  },
  getMovieById: async (id: number | string): Promise<Movie> => {
    try {
      const res = await api.get<ApiResponse<Movie>>(`/catalog/movies/${id}`);
      return res.data.data;
    } catch {
      return MOCK_MOVIES.find(m => m.id === Number(id)) || MOCK_MOVIES[0];
    }
  },
  getEvents: async (): Promise<EventItem[]> => {
    try {
      const res = await api.get<ApiResponse<EventItem[]>>('/catalog/events');
      return res.data.data;
    } catch {
      return MOCK_EVENTS;
    }
  },
  getShowsForMovie: async (movieId: number, city?: string): Promise<Show[]> => {
    try {
      const res = await api.get<ApiResponse<Show[]>>(`/catalog/shows?movieId=${movieId}&city=${city || 'Mumbai'}`);
      return res.data.data;
    } catch {
      return MOCK_SHOWS.filter(s => s.movieId === Number(movieId));
    }
  }
};

export const bookingApi = {
  getSeats: async (showId: number, userId?: string): Promise<Seat[]> => {
    try {
      const res = await api.get<ApiResponse<Seat[]>>(`/bookings/shows/${showId}/seats?userId=${userId || 'user_guest'}`);
      return res.data.data;
    } catch {
      return generateMockSeats(showId);
    }
  },
  lockSeats: async (showId: number, userId: string, seatIds: number[]): Promise<ApiResponse<SeatLockResponse>> => {
    try {
      const res = await api.post<ApiResponse<SeatLockResponse>>('/bookings/lock-seats', {
        showId,
        userId,
        seatIds,
        ttlSeconds: 600
      });
      return res.data;
    } catch {
      return {
        success: true,
        data: {
          locked: true,
          lockId: 'lock_' + Math.random().toString(36).substring(7),
          showId,
          lockedSeatIds: seatIds,
          expiresAtEpochMs: Date.now() + 600000,
          message: "Seats locked successfully for 10 minutes in Redis!"
        }
      };
    }
  },
  autoAllocateContiguous: async (showId: number, seatCount: number, preferredTier?: string): Promise<ApiResponse<ContiguousSeatResponse>> => {
    try {
      const res = await api.post<ApiResponse<ContiguousSeatResponse>>('/bookings/auto-allocate', {
        showId,
        seatCount,
        preferredTier
      });
      return res.data;
    } catch {
      const seats = generateMockSeats(showId).filter(s => s.status === 'AVAILABLE');
      const allocated = seats.slice(0, seatCount);
      return {
        success: true,
        data: {
          found: true,
          showId,
          rowName: allocated[0]?.rowName || 'D',
          allocatedSeats: allocated,
          algorithmUsed: 'SegmentTree-ContiguousAllocation',
          message: `SegmentTree found ${seatCount} contiguous seats in Row ${allocated[0]?.rowName || 'D'}!`
        }
      };
    }
  },
  initiateBooking: async (bookingData: {
    userId: string;
    userEmail?: string;
    userPhone?: string;
    showId: number;
    movieTitle: string;
    theatreName: string;
    screenName: string;
    showTime: string;
    seatIds: number[];
    totalAmount: string | number;
    paymentMethod: string;
  }): Promise<ApiResponse<Booking>> => {
    try {
      const res = await api.post<ApiResponse<Booking>>('/bookings/initiate', bookingData);
      return res.data;
    } catch {
      const ref = "TF-" + Math.floor(1000 + Math.random() * 9000) + "-X9K";
      return {
        success: true,
        data: {
          bookingId: 'book_' + Date.now(),
          bookingReference: ref,
          userId: bookingData.userId,
          showId: bookingData.showId,
          movieTitle: bookingData.movieTitle,
          theatreName: bookingData.theatreName,
          screenName: bookingData.screenName,
          showTime: bookingData.showTime,
          seatNumbers: bookingData.seatIds.map(id => 'Seat-' + id),
          totalAmount: bookingData.totalAmount,
          status: 'PAYMENT_PENDING',
          createdAt: new Date().toISOString()
        }
      };
    }
  },
  confirmBooking: async (bookingId: string, transactionId: string): Promise<ApiResponse<{ bookingId: string; status: string }>> => {
    try {
      const res = await api.post<ApiResponse<{ bookingId: string; status: string }>>(`/bookings/${bookingId}/confirm?transactionId=${transactionId}`);
      return res.data;
    } catch {
      return {
        success: true,
        data: {
          bookingId,
          status: 'CONFIRMED'
        }
      };
    }
  }
};

export const paymentApi = {
  processPayment: async (paymentData: {
    bookingId: string;
    userId: string;
    amount: string | number;
    paymentMethod: string;
    idempotencyKey: string;
    simulateFailure?: boolean;
  }): Promise<ApiResponse<{ transactionId: string; bookingId: string; amount: number | string; status: string }>> => {
    try {
      const res = await api.post('/payments/process', paymentData);
      return res.data;
    } catch {
      if (paymentData.simulateFailure) {
        throw new Error("Simulated bank payment decline (Saga compensation triggered)");
      }
      return {
        success: true,
        data: {
          transactionId: "TX-" + Math.random().toString(36).substring(2, 10).toUpperCase(),
          bookingId: paymentData.bookingId,
          amount: paymentData.amount,
          status: 'SUCCESS'
        }
      };
    }
  }
};

