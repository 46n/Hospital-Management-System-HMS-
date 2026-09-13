package hms.manager;

import hms.data.TextFileUtil;
import hms.model.AdminStaff;
import hms.model.InsuranceNetwork;
import hms.model.User;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Coordinates accepted insurance network configuration, backed by insurance_networks.txt. */
public class InsuranceNetworkManager {

    private static final String DATA_FILE = "data/insurance_networks.txt";
    private static final String HEADER =
            "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId";

    private final List<InsuranceNetwork> networks = new ArrayList<>();
    private final UserManager userManager;
    private final File dataFile;

    public InsuranceNetworkManager(UserManager userManager) {
        this(userManager, new File(DATA_FILE));
    }

    public InsuranceNetworkManager(UserManager userManager, File dataFile) {
        if (userManager == null) {
            throw new IllegalArgumentException("User manager is required");
        }
        this.userManager = userManager;
        this.dataFile = dataFile;
        load();
    }

    public InsuranceNetwork createNetwork(String providerName, String planName, double coveragePercentage,
            String managedByStaffId) {
        ValidationUtil.requireNonBlank(providerName, "Provider name");
        ValidationUtil.requireNonBlank(planName, "Plan name");
        validateCoverage(coveragePercentage);
        AdminStaff staff = requireAdminStaff(managedByStaffId);

        InsuranceNetwork network = new InsuranceNetwork(
                IdGenerator.next("IN", collectIds()), providerName, planName, coveragePercentage, true);
        network.setManagedBy(staff);
        networks.add(network);
        save();
        return network;
    }

    public void updateNetwork(String networkId, String providerName, String planName, double coveragePercentage) {
        InsuranceNetwork network = requireNetwork(networkId);
        ValidationUtil.requireNonBlank(providerName, "Provider name");
        ValidationUtil.requireNonBlank(planName, "Plan name");
        validateCoverage(coveragePercentage);
        network.setProviderName(providerName);
        network.setPlanName(planName);
        network.setCoveragePercentage(coveragePercentage);
        save();
    }

    public void setActive(String networkId, boolean active) {
        InsuranceNetwork network = requireNetwork(networkId);
        network.setActive(active);
        save();
    }

    public void deleteNetwork(String networkId) {
        InsuranceNetwork network = requireNetwork(networkId);
        networks.remove(network);
        save();
    }

    public InsuranceNetwork findById(String networkId) {
        if (networkId == null) {
            return null;
        }
        for (InsuranceNetwork network : networks) {
            if (networkId.equals(network.getNetworkId())) {
                return network;
            }
        }
        return null;
    }

    public List<InsuranceNetwork> getNetworks() {
        return new ArrayList<>(networks);
    }

    private void validateCoverage(double coveragePercentage) {
        if (coveragePercentage < 0 || coveragePercentage > 100) {
            throw new IllegalArgumentException("Coverage percentage must be between 0 and 100");
        }
    }

    private AdminStaff requireAdminStaff(String staffId) {
        User user = userManager.findById(staffId);
        if (!(user instanceof AdminStaff)) {
            throw new IllegalArgumentException("Admin Staff not found: " + staffId);
        }
        return (AdminStaff) user;
    }

    private InsuranceNetwork requireNetwork(String networkId) {
        InsuranceNetwork network = findById(networkId);
        if (network == null) {
            throw new IllegalArgumentException("Insurance network not found: " + networkId);
        }
        return network;
    }

    private List<String> collectIds() {
        List<String> ids = new ArrayList<>();
        for (InsuranceNetwork network : networks) {
            ids.add(network.getNetworkId());
        }
        return ids;
    }

    private void load() {
        for (String line : TextFileUtil.readLines(dataFile)) {
            InsuranceNetwork network = parseLine(line);
            if (network != null) {
                networks.add(network);
            }
        }
    }

    private InsuranceNetwork parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 6) {
            System.err.println("Skipping malformed insurance network record: " + line);
            return null;
        }
        try {
            InsuranceNetwork network = new InsuranceNetwork(fields[0], fields[1], fields[2],
                    Double.parseDouble(fields[3]), Boolean.parseBoolean(fields[4]));
            if (!fields[5].isEmpty()) {
                User user = userManager.findById(fields[5]);
                if (user instanceof AdminStaff) {
                    network.setManagedBy((AdminStaff) user);
                }
            }
            return network;
        } catch (NumberFormatException e) {
            System.err.println("Skipping insurance network record with invalid data: " + line);
            return null;
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (InsuranceNetwork network : networks) {
            lines.add(toLine(network));
        }
        TextFileUtil.writeLines(dataFile, HEADER, lines);
    }

    private String toLine(InsuranceNetwork network) {
        return String.join("|",
                network.getNetworkId(),
                TextFileUtil.emptyIfNull(network.getProviderName()),
                TextFileUtil.emptyIfNull(network.getPlanName()),
                String.valueOf(network.getCoveragePercentage()),
                String.valueOf(network.isActive()),
                network.getManagedBy() == null ? "" : network.getManagedBy().getStaffId());
    }
}
