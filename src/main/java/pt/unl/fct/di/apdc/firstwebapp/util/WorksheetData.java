package pt.unl.fct.di.apdc.firstwebapp.util;

public class WorksheetData {

    public AuthToken authToken;
    public String reference;
    public String description;
    public String propertyType;
    public String status;

    // Fields for "AWARDED" status
    public String awardDate;
    public String startDate;
    public String endDate;
    public String partnerAccount;
    public String companyName;
    public String companyNIF;
    public String workStatus;
    public String observations;

    // Default constructor for JSON deserialization
    public WorksheetData() {}

    // Check if mandatory fields are valid
    public boolean isValid() {
        return reference != null && !reference.isEmpty()
                && description != null && !description.isEmpty()
                && propertyType != null && !propertyType.isEmpty()
                && status != null && !status.isEmpty();
    }

    // Check if "AWARDED" fields are valid (only if status is "AWARDED")
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
        return true; // If status is not "AWARDED", these fields are not required
    }
}