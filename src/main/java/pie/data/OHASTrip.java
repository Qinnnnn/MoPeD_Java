package pie.data;

public class OHASTrip {
    private int id;
    private String income;
    private String gender;
    private String age;
    private String mode;
    private int paz;
    private double hhDensity;
    private double totalJobDensity;
    private double uliJobDensity;
    private int fourWayIntersection;
    private double weight;
    private String purpose;
    private double distance;
    private String disable;
    private String drivingLicense;
    private String transitPass;

    public OHASTrip(int id, String income, String gender, String age, String mode, int pazId, double weight, String purpose, double distance) {
        this.id = id;
        this.income = income;
        this.gender = gender;
        this.age = age;
        this.mode = mode;
        this.paz = pazId;
        this.weight = weight;
        this.purpose = purpose;
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIncome() {
        return income;
    }

    public void setIncome(String income) {
        this.income = income;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getPaz() {
        return paz;
    }

    public void setPaz(int paz) {
        this.paz = paz;
    }

    public double getHhDensity() {
        return hhDensity;
    }

    public void setHhDensity(double hhDensity) {
        this.hhDensity = hhDensity;
    }

    public double getTotalJobDensity() {
        return totalJobDensity;
    }

    public void setTotalJobDensity(double totalJobDensity) {
        this.totalJobDensity = totalJobDensity;
    }

    public double getUliJobDensity() {
        return uliJobDensity;
    }

    public void setUliJobDensity(double uliJobDensity) {
        this.uliJobDensity = uliJobDensity;
    }

    public int getFourWayIntersection() {
        return fourWayIntersection;
    }

    public void setFourWayIntersection(int fourWayIntersection) {
        this.fourWayIntersection = fourWayIntersection;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDisable() {
        return disable;
    }

    public void setDisable(String disable) {
        this.disable = disable;
    }

    public String getDrivingLicense() {
        return drivingLicense;
    }

    public void setDrivingLicense(String drivingLicense) {
        this.drivingLicense = drivingLicense;
    }

    public String getTransitPass() {
        return transitPass;
    }

    public void setTransitPass(String transitPass) {
        this.transitPass = transitPass;
    }
}
