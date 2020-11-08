package org.soaringforecast.rasp.one800wxbrief.options;

import java.util.ArrayList;

import static org.soaringforecast.rasp.one800wxbrief.WxBriefViewModel.TypeOfBrief;


/**
 * Used to contain all briefing options to be displayed and selectable by user to create a route briefing request.
 */
public class BriefingOptions {

    private ArrayList<BriefingOption> productCodes;
    private ArrayList<BriefingOption> tailoringOptions;

    //A subset of the full productCodes that are pertinient to the type of brief
    private ArrayList<String> displayableProductCodes = new ArrayList<>();
    // only associated products with a 'true' value are to be included in the wxbrief api call
    private ArrayList<Boolean> productCodeIsChecked = new ArrayList<>();
    private ArrayList<Integer> productCodeListIndex = new ArrayList<>();

    //A subset of the full tailoringOptions that are pertinient to the type of brief
    private ArrayList<String> displayableTailoringOptions = new ArrayList<>();
    private ArrayList<Boolean> tailoringOptionIsChecked = new ArrayList<>();
    private ArrayList<Integer> tailoringOptionListIndex = new ArrayList<>();

    TypeOfBrief typeOfBrief;


    private BriefingOptions(){};

    /**
     *
     * @param productCodes
     * @param tailoringOptions - may be NGBV2 or non-NGBV2 options
     * @param typeOfBrief
     */
    public BriefingOptions(ArrayList<BriefingOption> productCodes, ArrayList<BriefingOption> tailoringOptions, TypeOfBrief typeOfBrief){
        this.productCodes = productCodes;
        this.tailoringOptions = tailoringOptions;
        this.typeOfBrief = typeOfBrief;

    }
    private void createDisplayFields() {
        switch (typeOfBrief) {
            case STANDARD:
                createStandardProductCodeOptions();
                createStandardTailoringOptions();
                break;
//            case OUTLOOK:
//                createOutlookOptions();
//                break;
//            case ABBREVIATED:
//                createAbbreviatedOptions();
//                break;
        }
    }

    private void createAbbreviatedOptions() {

    }

    private void createOutlookOptions() {
    }

    /**
     * Create a list of product codes/descriptions that can be include in a brief
     */
    private void createStandardProductCodeOptions() {
        BriefingOption briefingOption;
        displayableProductCodes.clear();
        productCodeIsChecked.clear();
        productCodeListIndex.clear();
        for (int i = 0; i < productCodes.size() - 1; ++i) {
            if (productCodes.get(i).isStandardBriefOption()) {
                briefingOption = productCodes.get(i);
                displayableProductCodes.add(briefingOption.getDisplayDescription());
                productCodeIsChecked.add(briefingOption.isSelectForStandardBrief());
                productCodeListIndex.add(i);
            }
        }
    }

    // Display a list of tailoring options to include (do not send EXCLUDE key value)  in a brief
    private void createStandardTailoringOptions() {
        BriefingOption briefingOption;
        displayableTailoringOptions.clear();
        tailoringOptionIsChecked.clear();
        tailoringOptionListIndex.clear();
        for (int i = 0; i < tailoringOptions.size() - 1; ++i) {
            if (tailoringOptions.get(i).isStandardBriefOption()) {
                briefingOption = tailoringOptions.get(i);
                displayableTailoringOptions.add(briefingOption.getDisplayDescription());
                tailoringOptionIsChecked.add(briefingOption.isSelectForStandardBrief());
                tailoringOptionListIndex.add(i);
            }
        }
    }

    /**
     * Update the list of product codes to be sent in a briefing request.
     * @param selectedProductCodes
     */
    private void updateProductCodesSelected(boolean[]selectedProductCodes ){
        if (selectedProductCodes.length == productCodeListIndex.size()){
            for (int i = 0; i < selectedProductCodes.length; ++i) {
                productCodeIsChecked.set(i,selectedProductCodes[i]);
            }
        }
    }

    /**
     * Update both the display list AND original list of tailoring options (as app can send EXCLUDE values
     * for those options not of interest to glider pilots (like inactive vors,...)
     * @param selectedTailoringOptions
     */
    private void updateTailoringOptionsSelected(boolean[] selectedTailoringOptions){
        BriefingOption briefingOption;
        if (selectedTailoringOptions.length == tailoringOptionListIndex.size()){
            for (int i = 0; i < selectedTailoringOptions.length; ++i) {
                tailoringOptionIsChecked.set(i,selectedTailoringOptions[i]);
                briefingOption = tailoringOptions.get(tailoringOptionListIndex.get(i));
                switch (typeOfBrief) {
                    case STANDARD:
                       briefingOption.setSelectForOutLookBrief(selectedTailoringOptions[i]);
                        break;
//            case OUTLOOK:
//                createOutlookOptions();
//                break;
//            case ABBREVIATED:
//                createAbbreviatedOptions();
//                break;
                }

            }

        }

    }

}
