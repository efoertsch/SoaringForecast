package org.soaringforecast.rasp.one800wxbrief.options;

import org.soaringforecast.rasp.common.Constants;

import java.util.ArrayList;
import java.util.List;



/**
 * Used to contain all briefing options to be displayed and selectable by user to create a route briefing request.
 */
public class BriefingOptions {

    // Full set of briefing options
    private ArrayList<BriefingOption> fullProductCodeList;
    //A subset of the full productCodes that are pertinent to the type of brief (Standard, Outlook, Abbreviated)
    private ArrayList<String> productCodeDescriptions = new ArrayList<>();
    // only associated products with a 'true' value are to be included in the wxbrief api call
    // same size as productCodeDescriptions
    private  boolean[]  productCodesSelected ;
    // index of this option back to the full set of productCodes
    // same size as productCodeDescriptions
    private ArrayList<Integer> productCodeListIndex = new ArrayList<>() ;

    // Full set of tailoringOptions
    private ArrayList<BriefingOption> fullTailoringOptionList;
    //A subset of the full tailoringOptions that are pertinent to the type of brief (Standard, Outlook, Abbreviated)
    private ArrayList<String> tailorOptionDescriptions = new ArrayList<>();
    // only associated options with a 'true' value are to be included in the wxbrief api call
    // same size as tailorOptionDescriptions
    private boolean[] tailoringOptionsSelected ;
    // index of this option back to the full set of tailoringOptions
    // same size as tailorOptionDescriptions
    private ArrayList<Integer>  tailoringOptionListIndex  = new ArrayList<>();

    Constants.TypeOfBrief typeOfBrief;

    private BriefingOptions(){};

    /**
     *
     * @param fullProductCodeList
     * @param fullTailoringOptionList - may be NGBV2 or non-NGBV2 options
     */
    public BriefingOptions(ArrayList<BriefingOption> fullProductCodeList, ArrayList<BriefingOption> fullTailoringOptionList){
        this.fullProductCodeList = fullProductCodeList;
        this.fullTailoringOptionList = fullTailoringOptionList;
        createProductCodeDisplayFields();
        createTailoringOptionsDisplayFields();


    }

    private void createProductCodeDisplayFields() {
        BriefingOption briefingOption;
        productCodeDescriptions.clear();
        productCodeListIndex.clear();
        ArrayList<Boolean> selectedList = new ArrayList<>();
        for (int i = 0; i < fullProductCodeList.size() - 1; ++i) {
            if (fullProductCodeList.get(i).isBriefOption()) {
                briefingOption = fullProductCodeList.get(i);
                productCodeDescriptions.add(briefingOption.getDisplayDescription());
                selectedList.add(briefingOption.isSelectForBrief());
                productCodeListIndex.add(i);
            }
        }
        productCodesSelected =  new boolean[selectedList.size()];
        for (int i = 0; i < productCodesSelected.length; ++i) {
            productCodesSelected[i] = selectedList.get(i);
        }
    }

    public List<String> getProductCodeDescriptionList() {
        return productCodeDescriptions;
    }


    public boolean[] getProductCodesSelected() {
        return productCodesSelected;
    }


    /**
     * Replace the list of product codes to be sent in a briefing request.
     * @param selectedProductCodes
     */
    public void updateProductCodesSelected(boolean[] selectedProductCodes ){
        if (selectedProductCodes.length == productCodesSelected.length){
            productCodesSelected = selectedProductCodes;
        }
    }

    // Display a list of tailoring options to include (do not send EXCLUDE key value)  in a brief
    private void createTailoringOptionsDisplayFields() {
        BriefingOption briefingOption;
        tailorOptionDescriptions.clear();
        tailoringOptionListIndex.clear();
        ArrayList<Boolean> selectedList = new ArrayList<>();
        for (int i = 0; i < fullTailoringOptionList.size() - 1; ++i) {
            if (fullTailoringOptionList.get(i).isBriefOption()) {
                briefingOption = fullTailoringOptionList.get(i);
                tailorOptionDescriptions.add(briefingOption.getDisplayDescription());
                selectedList.add(briefingOption.isSelectForBrief());
                tailoringOptionListIndex.add(i);
            }
        }
        tailoringOptionsSelected = new boolean[selectedList.size()];
        for (int i = 0; i < tailoringOptionsSelected.length; ++i) {
            tailoringOptionsSelected[i] = selectedList.get(i);
        }

    }




    /**
     * Update both the display list AND original list of tailoring options (as app can send EXCLUDE values
     * for those options not of interest to glider pilots (like inactive vors,...)
     * @param selectedTailoringOptions
     */
    public void updateTailoringOptionsSelected(boolean[] selectedTailoringOptions){
        BriefingOption briefingOption;
        if (selectedTailoringOptions.length == tailoringOptionsSelected.length){
            tailoringOptionsSelected  = selectedTailoringOptions;
            for (int i = 0; i < selectedTailoringOptions.length; ++i) {
                briefingOption = fullTailoringOptionList.get(tailoringOptionListIndex.get(i));
                       briefingOption.setSelectForBrief(selectedTailoringOptions[i]);

            }
        }
    }

    public List<String> getTailoringOptionDescriptions() {
        return tailorOptionDescriptions;
    }

    public boolean[] getSelectedTailoringOptions() {
        return tailoringOptionsSelected;
    }



}
