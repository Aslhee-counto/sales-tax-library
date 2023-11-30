package com.xion.services;


import com.xion.components.ActivationRecordRepository;
import com.xion.components.ListingRepository;
import com.xion.data.F5Form;
import com.xion.exceptions.GstException;
import com.xion.models.gst.ActivationRecord;
import com.xion.models.gst.GstStatus;
import com.xion.models.gst.Listing;
import com.xion.repositories.GstRepository;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Service
public class StoreService {

    private static Logger logger = Logger.getLogger(StoreService.class.getName());

    private GstRepository gstRepository;
    @Autowired  private ListingRepository listingRepository;
    @Autowired  private ActivationRecordRepository activationRecordRepository;

//    //TODO >> F5 Form
    public F5Form loadF5Form(String legalName, String uuid) throws GstException {
        logger.info("Attempting to load F5 form for company " + legalName + " and uuid " + uuid);
        try {
            F5Form form = null;

            boolean retry = true;
            int attempt = 1;
            while(retry) {
                try {
                    form = gstRepository.loadF5(legalName, uuid);
                    break;
                }catch (Exception e){
                    if (attempt<3) {
                        logger.warning("attempt " + attempt + " failed");
                    }else {
                        logger.severe("attempt " + attempt + " failed");
                        throw e;
                    }
                    attempt++;
                }
            }
            logger.info("Form from StoreService: " + form);
            return form;
        }catch (Exception e){
            logger.severe("Error loading F5 for " + legalName + " and uuid " + uuid);
            throw new GstException(e);

        }
    }

    public boolean storeF5Form(String legalName, String uuid, F5Form form){
        logger.info("Attempting to store F5 for company " + legalName + " and uuid " + uuid);
        try {
            logger.info("about to store F5, form has " + form.getTransactions().size() + " transactions");
            return gstRepository.storeF5(legalName, uuid, form);
        }catch (Exception e){
            logger.severe("Could not store F5 for company " + legalName + " and uuid " + uuid);
            return false;
        }
    }

    public Resource loadF5PDF(String legalName, String uuid) throws GstException {
        logger.info("Attempting to load F5 pdf for company " + legalName + " and uuid " + uuid);
        try {
            logger.info("GST REPOSITORY FPR LOADF5PDF: " + gstRepository.loadF5PDF(legalName,uuid));
            return gstRepository.loadF5PDF(legalName,uuid);
        }catch (Exception e){
            logger.severe("Error loading F5 for " + legalName + " and uuid " + uuid);
            throw new GstException(e);

        }
    }

    public boolean storeF5PDF(String legalName, String uuid, InputStream pdf){
        logger.info("Attempting to store F5 for company " + legalName + " and uuid " + uuid);
        try {
            return gstRepository.storeF5PDF(legalName, uuid, pdf);
        }catch (Exception e){
            logger.severe("Could not store F5 for company " + legalName + " and uuid " + uuid);
            return false;
        }
    }

    public Resource loadTransactionsXlsl(String legalName, String uuid) throws GstException {
        logger.info("Attempting to load xlsx for company " + legalName + " and uuid " + uuid);
        try {
            return gstRepository.loadTransactionsXlsx(legalName,uuid);
        }catch (Exception e){
            logger.severe("Error loading xlsx for " + legalName + " and uuid " + uuid);
            throw new GstException(e);

        }
    }

    public boolean storeTransactionsXlsl(String legalName, String uuid, Workbook workbook){
        logger.info("Attempting to store xlsx for company " + legalName + " and uuid " + uuid);
        try {
            return gstRepository.storeTransactionsXlsx(legalName, uuid, workbook);
        }catch (Exception e){
            logger.severe("Could not store xlsx for company " + legalName + " and uuid " + uuid);
            return false;
        }
    }

    public boolean checkF5Exists(String legalName, String uuid) throws GstException {
        logger.info("Checking F5 for company " + legalName + " and uuid " + uuid);
        try {
            return gstRepository.checkF5Exists(legalName,uuid, ".json");
        }catch (Exception e){
            logger.severe("Error checking F5 for " + legalName + " and uuid " + uuid);
            throw new GstException(e);
        }
    }

    @Transactional //2
    public List<Listing> loadListings(String legalName) throws GstException {
        logger.info("Attempting to load listings for company " + legalName);
        try {
            List<Listing> listings = listingRepository.findAllByLegalName(legalName);
            logger.info("Loaded listings for " + legalName);
            logger.info("-------------------------------");
            logger.info("-------------------------------");
            logger.info("-------------------------------");
            logger.info("Listings: " + listings.toString());

            Map<String, Listing> uniqueListingsByStartDate = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, Listing> currentPeriodListings = new HashMap<>();
            List<Listing> dueListings = new ArrayList<>();



            for (Listing listing : listings) {
                String startDateKey = sdf.format(listing.getPeriodStart());
                GstStatus status = listing.getStatus();

                // Suriin kung mayroon nang listing para sa startDateKey.
                Listing existingListing = uniqueListingsByStartDate.get(startDateKey);

                // Kung wala pang existing listing, o kung ang listing ay CurrentPeriod, ilagay ito sa map.
                if (existingListing == null || GstStatus.CurrentPeriod.equals(status)) {
                    uniqueListingsByStartDate.put(startDateKey, listing);



                } else if (GstStatus.OverDue.equals(status)) {
                    // Kung ang status ay Due o OverDue, ilagay lamang kung walang umiiral na CurrentPeriod.
                    if (!GstStatus.CurrentPeriod.equals(existingListing.getStatus())) {
                        uniqueListingsByStartDate.put(startDateKey, listing);
                    } else {
                        uniqueListingsByStartDate.remove(startDateKey, listing);
                    }

                } else if (GstStatus.Due.equals(status)) {
                    // Kung ang status ay Due o OverDue, ilagay lamang kung walang umiiral na CurrentPeriod.
                    if (!GstStatus.CurrentPeriod.equals(existingListing.getStatus())) {
                        uniqueListingsByStartDate.put(startDateKey, listing);
                    } else {
                        uniqueListingsByStartDate.remove(startDateKey, listing);
                    }
                }
//                if (listing.getStatus().equals(GstStatus.Due)) {
//                    dueListings.add(listing);
//                }
            }


            List<Listing> filteredListings = new ArrayList<>(uniqueListingsByStartDate.values());
            logger.info("Filtered listings: " + filteredListings.toString());

            checkForMultipleCurrentPeriods(filteredListings);

//            Listing uniqueListingsByStartDate = dueListings.stream()
//                    .max(Comparator.comparing(Listing::getPeriodStart))
//                    .orElse(null);
//            if (uniqueDueListing != null && !uniqueListingsByStartDate.containsKey(uniqueDueListing.getPeriodStart().toString())) {
//                filteredListings.add(uniqueDueListing);
//            }

            return filteredListings;
        } catch (Exception e) {
            logger.severe("Could not pull listings for " + legalName);
            throw new GstException(e);
        }
    }

    public void checkForMultipleCurrentPeriods(List<Listing> filteredListings) {
        long currentPeriodCount = filteredListings.stream()
                .filter(listing -> GstStatus.CurrentPeriod.equals(listing.getStatus()))
                .count();

        logger.info("Number of CurrentPeriod listings: " + currentPeriodCount);

        if (currentPeriodCount > 1) {
            logger.info("More than one CurrentPeriod listing found.");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    
            Listing mostRecentListing = filteredListings.stream()
                    .filter(listing -> GstStatus.CurrentPeriod.equals(listing.getStatus()))
                    .min(Comparator.comparing(Listing::getPeriodEnd))
                    .orElse(null);

            if (mostRecentListing != null) {
                // Alisin ang iba pang CurrentPeriod listings maliban sa pinaka-recent.
                filteredListings.removeIf(listing -> GstStatus.CurrentPeriod.equals(listing.getStatus()) && !listing.equals(mostRecentListing));

                logger.info("Retained the most recent CurrentPeriod listing and removed the others.");
            }


        } else if (currentPeriodCount == 0) {
            logger.info("No CurrentPeriod listings found.");
        } else {
            logger.info("Only one CurrentPeriod listing present, no action needed.");


        }
    }





    @Transactional //3
    public Listing loadListingById(long id) throws GstException {
        logger.info("Attempting to load listing for id " + id);
        try {
            Listing listings = listingRepository.findById(id).orElse(null);
            logger.info("Loaded listings for " + id);
            return listings;
        }catch (NullPointerException e){
            logger.severe("No listings for " + id);
            throw new GstException(e);
        }catch (Exception e){
            logger.severe("Could not pull listings for " + id);
            throw new GstException(e);
        }
    }

    @Transactional //4
    public boolean storeListing(Listing listing) {
        logger.info("Attempting to store listing " + listing.toString()); //number of listed
        try {
            listingRepository.save(listing);
            logger.info("Stored listing " + listing.toString()); //number of listed
            return true;
        }catch (Exception e){
            logger.severe("Could not store listing " + listing.toString());
            return false;
        }
    }

    @Transactional
    public boolean storeListings(List<Listing> listings) {
        logger.info("Attempting to store listings: " + listings.size());
        try {
            listingRepository.saveAll(listings);
            logger.info("Stored listings: " + listings.size());
            return true;
        }catch (Exception e){
            logger.severe(e.getMessage());
            logger.severe("Could not store listings: " + listings.toString());
            return false;
        }
    }

    @Transactional
    public boolean deleteListing(Listing listing) {
        logger.info("Attempting to delete listing " + listing.toString());
        try {
            listingRepository.delete(listing);
            logger.info("deleted listing " + listing.toString());
            return true;
        }catch (Exception e){
            logger.severe("Could not delete listing " + listing.toString());
            return false;
        }
    }

    @Transactional
    public boolean storeActivationRecord(ActivationRecord record) {
        logger.info("Attempting to store activation record for " + record.getEmail());
        logger.info("==record: " + record. toString());
        try {
            activationRecordRepository.save(record);
            logger.info("Stored activation record for " + record.getEmail());
            logger.info("***record: " + record. toString());

            return true;
        }catch (Exception e){
            logger.severe("Could not store activation record for " + record.getEmail());
            return false;
        }
    }

    @Transactional
    public boolean deleteActivationRecord(ActivationRecord record) {
        logger.info("Attempting to delete activation record for " + record.getEmail());
        logger.info("Attempting to delete activation record for " + record.toString());
        try {
            activationRecordRepository.delete(record);
            logger.info("deleted activation record for " + record.getEmail());
            return true;
        }catch (Exception e){
            logger.severe("Could not delete activation record for " + record.getEmail());
            return false;
        }
    }

    @Transactional
    public ActivationRecord loadActivationRecord(String legalName) throws Exception{
        logger.info("Attempting to load activation record for " + legalName);
        try {
            ActivationRecord record = activationRecordRepository.findByLegalName(legalName);
            logger.info("Loaded activation record for " + legalName);
            logger.info("Records(for Testing) " + record);

            return record;
        }catch (Exception e){
            logger.severe("Could not load activation record for " + legalName);
            logger.severe(e.getMessage());
            throw e;
        }
    }
}
