package com.ticketflow.catalog.config;

import com.ticketflow.catalog.model.*;
import com.ticketflow.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final MovieRepository movieRepository;
    private final EventRepository eventRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final ShowRepository showRepository;

    @Override
    public void run(String... args) {
        if (movieRepository.count() > 0) {
            log.info("Catalog database already initialized.");
            return;
        }

        log.info("Seeding initial TicketFlow movies, events, theatres and shows...");

        Movie m1 = movieRepository.save(Movie.builder()
                .title("Dune: Part Two")
                .description("Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.")
                .genre("Sci-Fi / Adventure")
                .language("English, Hindi")
                .durationMinutes(166)
                .rating(8.8)
                .certificate("U/A")
                .posterUrl("https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80")
                .backdropUrl("https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80")
                .trailerUrl("https://www.youtube.com/watch?v=Way9Dexny3w")
                .releaseDate(LocalDate.now().minusDays(10))
                .isTrending(true)
                .build());

        Movie m2 = movieRepository.save(Movie.builder()
                .title("Oppenheimer")
                .description("The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.")
                .genre("Biography / Drama")
                .language("English, Hindi")
                .durationMinutes(180)
                .rating(8.9)
                .certificate("A")
                .posterUrl("https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=600&auto=format&fit=crop&q=80")
                .backdropUrl("https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=1200&auto=format&fit=crop&q=80")
                .trailerUrl("https://www.youtube.com/watch?v=uYPbbksJxIg")
                .releaseDate(LocalDate.now().minusDays(30))
                .isTrending(true)
                .build());

        Movie m3 = movieRepository.save(Movie.builder()
                .title("Interstellar")
                .description("When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot is tasked with piloting a spacecraft.")
                .genre("Sci-Fi / Drama")
                .language("English")
                .durationMinutes(169)
                .rating(8.7)
                .certificate("U/A")
                .posterUrl("https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80")
                .backdropUrl("https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1200&auto=format&fit=crop&q=80")
                .trailerUrl("https://www.youtube.com/watch?v=zSWdZVtXT7E")
                .releaseDate(LocalDate.now().minusDays(60))
                .isTrending(false)
                .build());

        Event e1 = eventRepository.save(Event.builder()
                .title("Coldplay: Music of the Spheres World Tour")
                .description("Experience an unforgettable night under the stars with Chris Martin and Coldplay live in concert.")
                .category("Music Concert")
                .performer("Coldplay")
                .venue("DY Patil Stadium")
                .city("Mumbai")
                .bannerUrl("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1000&auto=format&fit=crop&q=80")
                .eventDateTime(LocalDateTime.now().plusDays(15))
                .priceStartingFrom(new BigDecimal("2500.00"))
                .isFeatured(true)
                .build());

        Event e2 = eventRepository.save(Event.builder()
                .title("Zakir Khan - Tathastu Live Standup")
                .description("India's favorite storyteller is back with his signature blend of relatable humor and poetic emotion.")
                .category("Comedy")
                .performer("Zakir Khan")
                .venue("Jawaharlal Nehru Stadium")
                .city("Delhi-NCR")
                .bannerUrl("https://images.unsplash.com/photo-1585699324551-f6c309eedeca?w=1000&auto=format&fit=crop&q=80")
                .eventDateTime(LocalDateTime.now().plusDays(7))
                .priceStartingFrom(new BigDecimal("999.00"))
                .isFeatured(true)
                .build());

        // Theatres
        Theatre t1 = theatreRepository.save(Theatre.builder()
                .name("PVR P[XL] Phoenix Palladium")
                .brand("PVR")
                .city("Mumbai")
                .location("Lower Parel")
                .address("462, Senapati Bapat Marg, Lower Parel, Mumbai")
                .build());

        Theatre t2 = theatreRepository.save(Theatre.builder()
                .name("INOX Megaplex Inorbit Mall")
                .brand("INOX")
                .city("Mumbai")
                .location("Malad West")
                .address("Link Road, Malad West, Mumbai")
                .build());

        Theatre t3 = theatreRepository.save(Theatre.builder()
                .name("PVR Director's Cut Ambience Mall")
                .brand("PVR")
                .city("Delhi-NCR")
                .location("Vasant Kunj")
                .address("Nelson Mandela Road, Vasant Kunj, New Delhi")
                .build());

        // Screens
        Screen s1 = screenRepository.save(Screen.builder()
                .name("Screen 1 (IMAX Laser)")
                .screenFormat("IMAX 3D")
                .totalRows(8)
                .seatsPerRow(12)
                .totalCapacity(96)
                .theatre(t1)
                .build());

        Screen s2 = screenRepository.save(Screen.builder()
                .name("Screen 2 (Dolby Atmos)")
                .screenFormat("Dolby Atmos")
                .totalRows(8)
                .seatsPerRow(12)
                .totalCapacity(96)
                .theatre(t1)
                .build());

        // Shows
        showRepository.save(Show.builder()
                .movieId(m1.getId())
                .theatreId(t1.getId())
                .screenId(s1.getId())
                .movieTitle(m1.getTitle())
                .theatreName(t1.getName())
                .screenName(s1.getName())
                .city(t1.getCity())
                .showDate(LocalDate.now())
                .startTime(LocalTime.of(14, 30))
                .endTime(LocalTime.of(17, 15))
                .soundExperience("IMAX 12-Track Immersive Audio")
                .basePrice(new BigDecimal("420.00"))
                .build());

        showRepository.save(Show.builder()
                .movieId(m1.getId())
                .theatreId(t1.getId())
                .screenId(s1.getId())
                .movieTitle(m1.getTitle())
                .theatreName(t1.getName())
                .screenName(s1.getName())
                .city(t1.getCity())
                .showDate(LocalDate.now())
                .startTime(LocalTime.of(19, 00))
                .endTime(LocalTime.of(21, 45))
                .soundExperience("IMAX 12-Track Immersive Audio")
                .basePrice(new BigDecimal("490.00"))
                .build());

        showRepository.save(Show.builder()
                .movieId(m2.getId())
                .theatreId(t1.getId())
                .screenId(s2.getId())
                .movieTitle(m2.getTitle())
                .theatreName(t1.getName())
                .screenName(s2.getName())
                .city(t1.getCity())
                .showDate(LocalDate.now())
                .startTime(LocalTime.of(16, 00))
                .endTime(LocalTime.of(19, 00))
                .soundExperience("Dolby Atmos 64-Channel")
                .basePrice(new BigDecimal("350.00"))
                .build());

        log.info("Catalog seeding finished successfully!");
    }
}

