package com.xion.services;

import com.xion.models.gst.GstStatus;
import com.xion.models.gst.Listing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ListingService {
//TODO >> ORIGINAL VERSION BEFORE EDITING
    private static Logger logger = Logger.getLogger(ListingService.class.getName());

    @Autowired private StoreService storeService;

    public List<Listing> checkNewListing(List<Listing> listings){
        logger.info("checkNewListing called for " + listings.size() + " listings");
        logger.info("------------------");
        logger.info("listings: " + listings.toString());

        validateSorted(listings);
        List<Listing> sorted = listings.stream().sorted(
                Comparator.comparing(Listing::getPeriodEnd)
        ).collect(Collectors.toList());



            Listing last = sorted.get(sorted.size() - 1);

            DateFormat format = new SimpleDateFormat("dd-MM-yyyy");

            if (!(last.getPeriodEnd().compareTo(new Date()) > 0)) {
                logger.info("end date of most recent listing period is " +
                        format.format(last.getPeriodEnd()) + ", creating new listing period");
                List<Listing> newListings = new ArrayList<>();
                boolean isQuarterly = isQuarterly(last);
                if (isQuarterly)
                    newListings.add(getNextQuarterly(last));
                else
                    newListings.add(getNextMonthly(last));

                while (outOfDate(newListings.get(newListings.size() - 1))) {
                    checkOverdue(newListings.get(newListings.size() - 1));
                    if (isQuarterly)
                        newListings.add(getNextQuarterly(newListings.get(newListings.size() - 1)));
                    else
                        newListings.add(getNextMonthly(newListings.get(newListings.size() - 1)));
                }
                storeService.storeListings(newListings);
                listings.addAll(newListings);
            }

        logger.info("----This LISTINGGS: " + listings.toString());
        return listings;
    }

    public void adjustPeriodsForExistingListings(List<Listing> listings) {
        logger.info("**adjustPeriodsForExistingListings");
        for (Listing listing : listings) {
            Calendar calendar = GregorianCalendar.getInstance();

            // Adjust startPeriod to the first day of the month
            calendar.setTime(listing.getPeriodStart());
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            listing.setPeriodStart(calendar.getTime());

            // Adjust endPeriod to the last day of the month
            calendar.setTime(listing.getPeriodEnd());
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            listing.setPeriodEnd(calendar.getTime());

            // Adjust dueDate to the last day of the month
            calendar.setTime(listing.getDueDate());
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            listing.setDueDate(calendar.getTime());

//            logger.info("**Adjusted Listing: " + listing.toString());
        }
        // Save the adjusted listings back to the database or wherever they are stored
        storeService.storeListings(listings);
    }

    public boolean checkOverlaps(List<Listing> listings) {
        logger.info("**checkOverlaps");

        List<Listing> sortedListings = listings.stream()
                .sorted(Comparator.comparing(Listing::getPeriodEnd))
                .collect(Collectors.toList());

        boolean overlapFixed = false;
        Date today = new Date();

        Optional<Listing> firstOverdue = sortedListings.stream()
                .filter(listing -> listing.getStatus().equals(GstStatus.OverDue))
                .findFirst();

        if (firstOverdue.isPresent()) {
            Listing overdueListing = firstOverdue.get();
            Calendar cal = Calendar.getInstance();
            cal.setTime(overdueListing.getPeriodStart());
            int startMonth = cal.get(Calendar.MONTH);
            cal.setTime(overdueListing.getPeriodEnd());
            int endMonth = cal.get(Calendar.MONTH);


        for (int i = 0; i < sortedListings.size() - 1; i++) {
            Listing currentListing = sortedListings.get(i);
            Listing nextListing = sortedListings.get(i + 1);

            if (endMonth - startMonth == 2) {
                logger.info("First overdue listing is quarterly." + (endMonth - startMonth == 2));

                Optional<Listing> currentPeriodListing = sortedListings.stream()
                        .filter(listing -> listing.getStatus().equals(GstStatus.CurrentPeriod))
                        .findFirst();

//            if (isQuarterly(currentListing)) {
                if (currentListing.getStatus().equals(GstStatus.Due) &&
                        nextListing.getStatus().equals(GstStatus.CurrentPeriod)) {
                    logger.info("CURRENT PEERIIIIOOOOOD");
                    // There is an overlap with the current date, adjust the period of the next listing
//                    Calendar cal = Calendar.getInstance();
                    cal.setTime(today);
                    cal.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of the next month after today
//                cal.add(Calendar.MONTH, 1);
                    nextListing.setPeriodStart(cal.getTime());
                    cal.add(Calendar.MONTH, 2);

                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    nextListing.setPeriodEnd(cal.getTime());

                    // Adjust the dueDate to the last day of the month after the new periodEnd
                    cal.add(Calendar.MONTH, 1);
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    nextListing.setDueDate(cal.getTime());

                    logger.info("Overlap with current date detected and adjusted: " + nextListing.toString());
                    overlapFixed = true;
                    storeService.storeListings(sortedListings);
                } else {
                    logger.info("No current period listing found to adjust.");
                }
            } else {
                logger.info("End of the month is monthly");
                Listing currentPeriodListing = sortedListings.stream()
                        .filter(listing -> listing.getStatus().equals(GstStatus.CurrentPeriod))
                        .findFirst()
                        .orElse(null);

                if (currentPeriodListing != null) {
//                    Calendar cal = Calendar.getInstance();
                    cal.setTime(currentPeriodListing.getPeriodStart());
                    int startDay = cal.get(Calendar.DAY_OF_MONTH);
//                    int startMonth = cal.get(Calendar.MONTH);
                    int startYear = cal.get(Calendar.YEAR);

                    // Check if the period start is the first of the month and end is the last of the same month
                    if (startDay == 1) {
                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                        Date endOfMonth = cal.getTime();

                        // If the period end is not the last day of the start month, adjust it
                        if (!currentPeriodListing.getPeriodEnd().equals(endOfMonth)) {
                            logger.info("Adjusting Month==");
                            currentPeriodListing.setPeriodEnd(endOfMonth);

                            // Adjust the dueDate to the last day of the next month
                            cal.add(Calendar.MONTH, 1);
                            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                            currentPeriodListing.setDueDate(cal.getTime());

                            logger.info("Overlap with current date detected and adjusted: " + currentPeriodListing.toString());
                            overlapFixed = true;
                            storeService.storeListings(sortedListings);
                        } else {
                            logger.info("No adjustment needed for current period listing.");
                        }
                    }
                }
            }
        }
        } else {
                logger.info("No first overdue presented");
            }

        return overlapFixed;
    }


    public List<Listing> checkForStatusChange(List<Listing> listings){
        logger.info("**check for status change***");
        List<Listing> toUpdate = new ArrayList<>();
//        Date today = new Date();

        for (Listing listing : listings){
            if (listing.getStatus().equals(GstStatus.Submitted) || listing.getStatus().equals(GstStatus.PartiallySubmitted))
                continue;
            if (checkOverdue(listing)) {
                toUpdate.add(listing);
            }else if (checkDue(listing)) {
                toUpdate.add(listing);
            }
        }

        storeService.storeListings(toUpdate);
        return listings;
    }

    private boolean outOfDate(Listing listing){
        if (listing.getPeriodEnd().compareTo(new Date()) < 0)
            return true;
        return false;
    }

    private boolean checkOverdue(Listing listing){
        if (listing.getDueDate().compareTo(new Date()) < 0) {
            listing.setStatus(GstStatus.OverDue);
            return true;

        }
        return false;
    }

    private boolean checkDue(Listing listing){
        if (listing.getDueDate().compareTo(new Date()) > 0) {
            if (listing.getPeriodEnd().compareTo(new Date()) < 0){
                listing.setStatus(GstStatus.Due);
                return true;
            }
        }
        return false;
    }

    private boolean isQuarterly(Listing listing) {
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        startCal.setTime(listing.getPeriodStart());
        endCal.setTime(listing.getPeriodEnd());

        int startMonth = startCal.get(Calendar.MONTH);
        int endMonth = endCal.get(Calendar.MONTH);
        int startYear = startCal.get(Calendar.YEAR);
        int endYear = endCal.get(Calendar.YEAR);

        int totalMonths = ((endYear - startYear) * 12) + (endMonth - startMonth);

        // Ang quarterly period ay dapat na tumagal ng tatlong buwan.
        return totalMonths == 2;
    }

    private Listing getNextMonthly(Listing current){
        logger.info("--isMonthly");

        return getNext(current, 1);
    }

    private Listing getNextQuarterly(Listing current){
        return getNext(current, 3);
    }

    private Listing getNext(Listing current, int offset){
        logger.info("--getQuarterly");

        Listing newListing = new Listing();
        newListing.setId(generateUniqueId());
        newListing.setStatus(GstStatus.CurrentPeriod);
        newListing.setCurrency(current.getCurrency());
        newListing.setLegalName(current.getLegalName());
        newListing.setNetGST(0d);

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(current.getDueDate());
        calendar.add(Calendar.MONTH, offset);
        newListing.setDueDate(calendar.getTime());

        calendar.setTime(current.getPeriodStart());
        calendar.add(Calendar.MONTH, offset);
        newListing.setPeriodStart(calendar.getTime());

        calendar.setTime(current.getPeriodEnd());
        calendar.add(Calendar.MONTH, offset);
        newListing.setPeriodEnd(calendar.getTime());

        return newListing;

    }

    private long generateUniqueId() {
        long val = -1;
        do {
            val = UUID.randomUUID().getMostSignificantBits();
        } while (val < 0);
        return val;
    }

    public List<Listing> validateSorted(List<Listing> sorted) {
        logger.info("Checking of Overlapping" + sorted);

        Listing earliestListing = Collections.min(sorted, Comparator.comparing(Listing::getPeriodStart));

        boolean isEarliestListingQuarterly = isQuarterly(earliestListing);
        logger.info("The earliest listing period is " + (isEarliestListingQuarterly ? "quarterly." : "monthly."));

        for (int i = 0; i < sorted.size() - 1; i++) {
            Listing current = sorted.get(i);
            Listing next = sorted.get(i + 1);


            if (isEarliestListingQuarterly) {
                logger.info("Adjusting Dates For Quarterly");
                logger.warning("Due: " + current.getStatus().equals(GstStatus.Due));
                logger.warning("next: " + next.toString());

                if (current.getStatus().equals(GstStatus.Due) && next.getStatus().equals(GstStatus.CurrentPeriod)) {
                    if (current.getPeriodEnd().compareTo(next.getPeriodStart()) >= 0) {
                        logger.warning("Overlapping period detected. Adjusting...");

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(current.getDueDate());
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        next.setPeriodStart(cal.getTime());

                        adjustDatesForQuarterly(next);

                    } else{
                        if (current.getStatus().equals(GstStatus.Due)){
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(current.getDueDate());
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            next.setPeriodStart(cal.getTime());

                            adjustDatesForQuarterly(next);
                        }
                    }
                }

            } else {
                logger.info("Adjusting Dates For Monthly");

                adjustDatesForMonthly(next);
            }
        }
        return sorted;
    }
    private void adjustDatesForQuarterly(Listing listing) {
        logger.info("Quarterly Adjustment");
        Calendar cal = Calendar.getInstance();
        cal.setTime(listing.getPeriodStart());
        cal.add(Calendar.MONTH, 3);  // or different offset for different period types
        cal.add(Calendar.DATE, -1);
        listing.setPeriodEnd(cal.getTime());

        cal.setTime(listing.getPeriodEnd());
        cal.add(Calendar.MONTH, 1);  // set due date to the last day of next month
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        listing.setDueDate(cal.getTime());
    }

    private void adjustDatesForMonthly (Listing listing) {
        logger.info("Monthly Adjustment" );

        Calendar cal = Calendar.getInstance();
        cal.setTime(listing.getPeriodStart());
        cal.add(Calendar.MONTH, 1);  // or different offset for different period types
        cal.add(Calendar.DATE, -1);
        listing.setPeriodEnd(cal.getTime());

        cal.setTime(listing.getPeriodEnd());
        cal.add(Calendar.MONTH, 1);  // set due date to the last day of next month
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        listing.setDueDate(cal.getTime());
    }

}
