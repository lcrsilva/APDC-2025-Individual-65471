package pt.unl.fct.di.apdc.firstwebapp.util;

public class WorksheetData {

    public AuthToken authToken;
    public String reference;
    public String description;
    public String propertyType;
    public String status;


    public String awardDate;
    public String startDate;
    public String endDate;
    public String partnerAccount;
    public String companyName;
    public String companyNIF;
    public String workStatus;
    public String observations;


    public WorksheetData() {}


    public boolean isValid() {
        return reference != null && !reference.isEmpty()
                && description != null && !description.isEmpty()
                && propertyType != null && !propertyType.isEmpty()
                && status != null && !status.isEmpty();
    }


    public boolean isAwardedValid() {
        if ("AWARDED".equals(status)) {
            return awardDate != null && !awardDate.isEmpty()
                    && startDate != null && !startDate.isEmpty()
                    && endDate != null && !endDate.isEmpty()
                    && partnerAccount != null && !partnerAccount.isEmpty()
                    && companyName != null && !companyName.isEmpty()
                    && companyNIF != null && !companyNIF.isEmpty()
                    && workStatus != null && !workStatus.isEmpty()
                    && observations != null && !observations.isEmpty();
        }
        return true;
    }
}