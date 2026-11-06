package com.ticketflow.catalog.config;

import com.ticketflow.catalog.model.*;
import com.ticketflow.catalog.repository.*;
import com.ticketflow.common.enums.ScreenFormat;
import com.ticketflow.common.enums.SeatTier;
import com.ticketflow.common.enums.ShowStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final EventRepository eventRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final ScreenSeatRepository screenSeatRepository;
    private final ShowRepository showRepository;

    @Override
    public void run(String... args) {
        if (movieRepository.count() > 0) {
            log.info("Catalog database already contains seeded data.");
            return;
        }

        log.info("Seeding production-grade TicketFlow catalog: Multi-city theatres, screens, seat layouts & shows...");

        // 1. Movies
        Movie m1 = movieRepository.save(Movie.builder()
                .title("Dune: Part Two")
                .slug("dune-part-two")
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
                .slug("oppenheimer")
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
                .slug("interstellar")
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

        Movie m4 = movieRepository.save(Movie.builder()
                .title("Spider-Man: Across the Spider-Verse")
                .slug("spider-man-across-the-spider-verse")
                .description("Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its existence.")
                .genre("Animation / Action")
                .language("English, Hindi, Tamil")
                .durationMinutes(140)
                .rating(8.9)
                .certificate("U")
                .posterUrl("https://images.unsplash.com/photo-1635805737707-575885ab0820?w=600&auto=format&fit=crop&q=80")
                .backdropUrl("https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=1200&auto=format&fit=crop&q=80")
                .trailerUrl("https://www.youtube.com/watch?v=cqGjhVJWtEg")
                .releaseDate(LocalDate.now().minusDays(20))
                .isTrending(true)
                .build());

        // 2. Events
        eventRepository.save(Event.builder()
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

        eventRepository.save(Event.builder()
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

        // 3. Multi-City Theatres
        // Mumbai
        Theatre tMum1 = theatreRepository.save(Theatre.builder()
                .name("PVR P[XL] Phoenix Palladium")
                .brand("PVR")
                .city("Mumbai")
                .location("Lower Parel")
                .address("462, Senapati Bapat Marg, Lower Parel, Mumbai")
                .latitude(new BigDecimal("18.9950"))
                .longitude(new BigDecimal("72.8242"))
                .totalScreens(2)
                .build());

        Theatre tMum2 = theatreRepository.save(Theatre.builder()
                .name("INOX Megaplex Inorbit Mall")
                .brand("INOX")
                .city("Mumbai")
                .location("Malad West")
                .address("Link Road, Malad West, Mumbai")
                .latitude(new BigDecimal("19.1764"))
                .longitude(new BigDecimal("72.8347"))
                .totalScreens(1)
                .build());

        // Delhi-NCR
        Theatre tDel1 = theatreRepository.save(Theatre.builder()
                .name("PVR Director's Cut Ambience Mall")
                .brand("PVR")
                .city("Delhi-NCR")
                .location("Vasant Kunj")
                .address("Nelson Mandela Road, Vasant Kunj, New Delhi")
                .latitude(new BigDecimal("28.5412"))
                .longitude(new BigDecimal("77.1557"))
                .totalScreens(1)
                .build());

        // Bengaluru
        Theatre tBlr1 = theatreRepository.save(Theatre.builder()
                .name("PVR Forum IMAX")
                .brand("PVR")
                .city("Bengaluru")
                .location("Koramangala")
                .address("Hosur Road, Koramangala, Bengaluru")
                .latitude(new BigDecimal("12.9352"))
                .longitude(new BigDecimal("77.6111"))
                .totalScreens(1)
                .build());

        // Hyderabad
        Theatre tHyd1 = theatreRepository.save(Theatre.builder()
                .name("AMB Cinemas")
                .brand("AMB")
                .city("Hyderabad")
                .location("Gachibowli")
                .address("Sarath City Capital Mall, Gachibowli - Miyapur Rd, Hyderabad")
                .latitude(new BigDecimal("17.4577"))
                .longitude(new BigDecimal("78.3653"))
                .totalScreens(1)
                .build());

        // 4. Screens & Physical Seating Layout Templates (ScreenSeat)
        Screen sMum1 = createScreenWithSeats(tMum1, "Audi 1 (IMAX Laser)", ScreenFormat.IMAX_3D, 8, 12);
        Screen sMum2 = createScreenWithSeats(tMum1, "Audi 2 (Dolby Atmos)", ScreenFormat.DOLBY_ATMOS, 8, 12);
        Screen sMum3 = createScreenWithSeats(tMum2, "Screen 1 (Insignia 4DX)", ScreenFormat.FOUR_DX, 8, 12);
        Screen sDel1 = createScreenWithSeats(tDel1, "Director's Cut Screen 1", ScreenFormat.DOLBY_ATMOS, 8, 12);
        Screen sBlr1 = createScreenWithSeats(tBlr1, "IMAX Screen 1", ScreenFormat.IMAX_3D, 8, 12);
        Screen sHyd1 = createScreenWithSeats(tHyd1, "Screen 1 (Laser 4K)", ScreenFormat.DOLBY_ATMOS, 8, 12);

        // 5. Shows & Tier Pricing
        LocalDate today = LocalDate.now();

        // Mumbai Shows
        createShow(m1, tMum1, sMum1, today, LocalTime.of(14, 30), LocalTime.of(17, 15), "IMAX 12-Track Audio", ScreenFormat.IMAX_3D, new BigDecimal("490.00"));
        createShow(m1, tMum1, sMum1, today, LocalTime.of(19, 00), LocalTime.of(21, 45), "IMAX 12-Track Audio", ScreenFormat.IMAX_3D, new BigDecimal("550.00"));
        createShow(m2, tMum1, sMum2, today, LocalTime.of(16, 00), LocalTime.of(19, 00), "Dolby Atmos 64-Channel", ScreenFormat.DOLBY_ATMOS, new BigDecimal("380.00"));
        createShow(m4, tMum2, sMum3, today, LocalTime.of(18, 30), LocalTime.of(20, 50), "4DX Motion Experience", ScreenFormat.FOUR_DX, new BigDecimal("420.00"));

        // Delhi Shows
        createShow(m1, tDel1, sDel1, today, LocalTime.of(15, 00), LocalTime.of(17, 45), "Dolby Atmos Luxury", ScreenFormat.DOLBY_ATMOS, new BigDecimal("580.00"));
        createShow(m2, tDel1, sDel1, today, LocalTime.of(20, 00), LocalTime.of(23, 00), "Dolby Atmos Luxury", ScreenFormat.DOLBY_ATMOS, new BigDecimal("580.00"));

        // Bengaluru Shows
        createShow(m1, tBlr1, sBlr1, today, LocalTime.of(14, 00), LocalTime.of(16, 45), "IMAX Laser 3D", ScreenFormat.IMAX_3D, new BigDecimal("450.00"));
        createShow(m3, tBlr1, sBlr1, today, LocalTime.of(18, 30), LocalTime.of(21, 20), "IMAX Laser 3D", ScreenFormat.IMAX_3D, new BigDecimal("420.00"));

        // Hyderabad Shows
        createShow(m1, tHyd1, sHyd1, today, LocalTime.of(16, 30), LocalTime.of(19, 15), "AMB Laser Immersive", ScreenFormat.DOLBY_ATMOS, new BigDecimal("400.00"));
        createShow(m4, tHyd1, sHyd1, today, LocalTime.of(20, 00), LocalTime.of(22, 20), "AMB Laser Immersive", ScreenFormat.DOLBY_ATMOS, new BigDecimal("350.00"));

        log.info("Catalog seeding finished successfully!");
    }

    private Screen createScreenWithSeats(Theatre theatre, String screenName, ScreenFormat format, int rows, int seatsPerRow) {
        Screen screen = screenRepository.save(Screen.builder()
                .theatre(theatre)
                .name(screenName)
                .screenFormat(format)
                .totalRows(rows)
                .seatsPerRow(seatsPerRow)
                .totalCapacity(rows * seatsPerRow)
                .build());

        List<ScreenSeat> seats = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            char rowChar = (char) ('A' + r);
            String rowName = String.valueOf(rowChar);

            // Tier assignment: A-B = SILVER, C-F = GOLD, G-H = PLATINUM
            SeatTier tier;
            if (r < 2) {
                tier = SeatTier.SILVER;
            } else if (r < 6) {
                tier = SeatTier.GOLD;
            } else {
                tier = SeatTier.PLATINUM;
            }

            for (int c = 1; c <= seatsPerRow; c++) {
                String seatNumber = rowName + c;
                boolean isAccessible = (r == 0 && (c == 1 || c == seatsPerRow));

                seats.add(ScreenSeat.builder()
                        .screen(screen)
                        .seatNumber(seatNumber)
                        .rowName(rowName)
                        .colIndex(c)
                        .defaultTier(tier)
                        .isAccessible(isAccessible)
                        .build());
            }
        }
        screenSeatRepository.saveAll(seats);
        return screen;
    }

    private void createShow(Movie movie, Theatre theatre, Screen screen, LocalDate date,
                            LocalTime start, LocalTime end, String sound, ScreenFormat format,
                            BigDecimal basePrice) {
        Show show = Show.builder()
                .movieId(movie.getId())
                .theatreId(theatre.getId())
                .screenId(screen.getId())
                .movieTitle(movie.getTitle())
                .theatreName(theatre.getName())
                .screenName(screen.getName())
                .city(theatre.getCity())
                .showDate(date)
                .startTime(start)
                .endTime(end)
                .soundExperience(sound)
                .screenFormat(format)
                .basePrice(basePrice)
                .status(ShowStatus.OPEN)
                .build();

        // Attach price tiers
        BigDecimal silverPrice = basePrice.multiply(new BigDecimal("0.75")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal goldPrice = basePrice;
        BigDecimal platinumPrice = basePrice.multiply(new BigDecimal("1.30")).setScale(2, RoundingMode.HALF_UP);

        ShowPriceTier tierSilver = ShowPriceTier.builder().show(show).tier(SeatTier.SILVER).price(silverPrice).build();
        ShowPriceTier tierGold = ShowPriceTier.builder().show(show).tier(SeatTier.GOLD).price(goldPrice).build();
        ShowPriceTier tierPlatinum = ShowPriceTier.builder().show(show).tier(SeatTier.PLATINUM).price(platinumPrice).build();

        show.setPriceTiers(new ArrayList<>(List.of(tierSilver, tierGold, tierPlatinum)));
        showRepository.save(show);
    }
}
