package org.soaringforecast.rasp.one800wxbrief.options;

import org.soaringforecast.rasp.common.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Used to contain all briefing options to be displayed and selectable by user to create a route briefing request.
 */
// TODO too much code duplication with the ViewModel. Reduce redundancy by moving code to ViewModel.
public class BriefingOptions {

    // Full set of briefing options
    private ArrayList<BriefingOption> fullProductCodeList;
    //A subset of the full productCodes that are pertinent to the type of brief (Standard, Outlook, Abbreviated)
    private ArrayList<String> productCodeDescriptions = new ArrayList<>();
    // only associated products with a 'true' value are to be included in the wxbrief api call
    // same size as productCodeDescriptions
    private boolean[] productCodesSelected;
    // index of this option back to the full set of productCodes
    // same size as productCodeDescriptions
    private ArrayList<Integer> productCodeListIndex = new ArrayList<>();

    // Full set of tailoringOptions
    private ArrayList<BriefingOption> fullTailoringOptionList;
    //A subset of the full tailoringOptions that are pertinent to the type of brief (Standard, Outlook, Abbreviated)
    private ArrayList<String> tailorOptionDescriptions = new ArrayList<>();
    // only associated options with a 'true' value are to be included in the wxbrief api call
    // same size as tailorOptionDescriptions
    private boolean[] tailoringOptionsSelected;
    // index of this option back to the full set of tailoringOptions
    // same size as tailorOptionDescriptions
    private ArrayList<Integer> tailoringOptionListIndex = new ArrayList<>();

    Constants.TypeOfBrief typeOfBrief;

    private BriefingOptions() { };

    /**
     * @param fullProductCodeList
     * @param fullTailoringOptionList - may be NGBV2
     */
    public BriefingOptions(ArrayList<BriefingOption> fullProductCodeList, ArrayList<BriefingOption> fullTailoringOptionList) {
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
        for (int i = 0; i < fullProductCodeList.size() ; ++i) {
            if (fullProductCodeList.get(i).isBriefOption()) {
                briefingOption = fullProductCodeList.get(i);
                productCodeDescriptions.add(briefingOption.getDisplayDescription());
                selectedList.add(briefingOption.isSelectForBrief());
                productCodeListIndex.add(i);
            }
        }
        productCodesSelected = new boolean[selectedList.size()];
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
     *
     * @param selectedProductCodes
     */
    public void updateProductCodesSelected(boolean[] selectedProductCodes) {
        if (selectedProductCodes.length == productCodesSelected.length) {
            productCodesSelected = selectedProductCodes;
        }
    }


    public ArrayList<String> getProductCodesForBriefing() {
        BriefingOption briefingOption;
        ArrayList<String> productCodesList = new ArrayList<>();

        int j = 0;
        for (int i = 0; i < fullProductCodeList.size() && j < productCodeListIndex.size(); ++i) {
            if (i == productCodeListIndex.get(j)) {
                // this option was one that was displayed to user to see what user wants
                if (productCodesSelected[j]) {
                    // User checked it so add the parm to the list
                    briefingOption = fullProductCodeList.get(i);
                    productCodesList.add(briefingOption.getWxBriefParameterName());
                } else {
                    // The user didn't select it so don't send the parm
                }
                ++j;
            } else {
                // On an option what wasn't displayable but
                // see if we should send it based on default value
                // true means send the parm value
                briefingOption = fullProductCodeList.get(i);
                if (briefingOption.isSelectForBrief()) {
                    productCodesList.add(briefingOption.getWxBriefParameterName());
                }
            }
        }
        return productCodesList;
    }




    // Display a list of tailoring options to include (do not send EXCLUDE key value)  in a brief
    private void createTailoringOptionsDisplayFields() {
        BriefingOption briefingOption;
        tailorOptionDescriptions.clear();
        tailoringOptionListIndex.clear();
        ArrayList<Boolean> selectedList = new ArrayList<>();
        for (int i = 0; i < fullTailoringOptionList.size() ; ++i) {
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


    public ArrayList<String> getTailoringOptionsForBriefing() {
        BriefingOption briefingOption;
        ArrayList<String> tailoringOptionList = new ArrayList<>();

        int j = 0;
        for (int i = 0; i < fullTailoringOptionList.size(); ++i) {
            if (j < tailoringOptionListIndex.size() && i == tailoringOptionListIndex.get(j)) {
                // this option was one that was displayed to user to see what user wants
                if (!tailoringOptionsSelected[j]) {
                    // User doesn't want to see this option so we add EXCLUDE value to list
                    briefingOption = fullTailoringOptionList.get(i);
                    tailoringOptionList.add(briefingOption.getWxBriefParameterName());
                }
                ++j;
            } else {
                // see if we should send it based on default value
                briefingOption = fullTailoringOptionList.get(i);
                if (!briefingOption.isSelectForBrief()) {
                    // Default is to not include this options so we add EXCLUDE value to list
                    tailoringOptionList.add(briefingOption.getWxBriefParameterName());
                }
            }
        }
        return tailoringOptionList;
    }

        /**
         * Update both the display list AND original list of tailoring options (as app can send EXCLUDE values
         * for those options not of interest to glider pilots (like inactive vors,...)
         *
         * @param selectedTailoringOptions
         */
    public void updateTailoringOptionsSelected(boolean[] selectedTailoringOptions) {
        BriefingOption briefingOption;
        if (selectedTailoringOptions.length == tailoringOptionsSelected.length) {
            tailoringOptionsSelected = selectedTailoringOptions;
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
