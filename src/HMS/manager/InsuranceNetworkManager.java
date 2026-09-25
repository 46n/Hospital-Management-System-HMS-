package hms.manager;

import hms.data.TextFileUtil;
import hms.model.AdminStaff;
import hms.model.InsuranceNetwork;
import hms.model.User;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
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
        ValidationUtil.requireValidPercentage(coveragePercentage, "Coverage percentage");
        String trimmedProvider = providerName.trim();
        String trimmedPlan = planName.trim();
        requireProviderPlanAvailable(trimmedProvider, trimmedPlan, null);
        AdminStaff staff = requireAdminStaff(managedByStaffId);

        InsuranceNetwork network = new InsuranceNetwork(
                IdGenerator.next("IN", collectIds()), trimmedProvider, trimmedPlan, coveragePercentage, true);
        network.setManagedBy(staff);
        networks.add(network);
        try {
            save();
        } catch (IllegalStateException e) {
            networks.remove(network);
            throw e;
        }
        return network;
    }

    public void updateNetwork(String networkId, String providerName, String planName, double coveragePercentage) {
        InsuranceNetwork network = requireNetwork(networkId);
        ValidationUtil.requireNonBlank(providerName, "Provider name");
        ValidationUtil.requireNonBlank(planName, "Plan name");
        ValidationUtil.requireValidPercentage(coveragePercentage, "Coverage percentage");
        String trimmedProvider = providerName.trim();
        String trimmedPlan = planName.trim();
        requireProviderPlanAvailable(trimmedProvider, trimmedPlan, networkId);

        String previousProvider = network.getProviderName();
        String previousPlan = network.getPlanName();
        double previousCoverage = network.getCoveragePercentage();
        network.setProviderName(trimmedProvider);
        network.setPlanName(trimmedPlan);
        network.setCoveragePercentage(coveragePercentage);
        try {
            save();
        } catch (IllegalStateException e) {
            network.setProviderName(previousProvider);
            network.setPlanName(previousPlan);
            network.setCoveragePercentage(previousCoverage);
            throw e;
        }
    }

    public void setActive(String networkId, boolean active) {
        InsuranceNetwork network = requireNetwork(networkId);
        boolean previousActive = network.isActive();
        network.setActive(active);
        try {
            save();
        } catch (IllegalStateException e) {
            network.setActive(previousActive);
            throw e;
        }
    }

    public void deleteNetwork(String networkId) {
        InsuranceNetwork network = requireNetwork(networkId);
        int previousIndex = networks.indexOf(network);
        networks.remove(network);
        try {
            save();
        } catch (IllegalStateException e) {
            networks.add(previousIndex, network);
            throw e;
        }
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

    /**
     * Rejects a provider+plan combination already used by a different network, ignoring case and
     * surrounding spaces. excludingNetworkId is null when creating (nothing to exclude) and the
     * network's own ID when updating (so a network may keep its own combination). Duplicates are
     * rejected outright, matching the duplicate-name pattern already used for hospital assets and
     * usernames elsewhere in this app.
     */
    private void requireProviderPlanAvailable(String trimmedProvider, String trimmedPlan, String excludingNetworkId) {
        for (InsuranceNetwork network : networks) {
            if (excludingNetworkId != null && network.getNetworkId().equals(excludingNetworkId)) {
                continue;
            }
            String existingProvider = network.getProviderName();
            String existingPlan = network.getPlanName();
            if (existingProvider != null && existingPlan != null
                    && existingProvider.trim().equalsIgnoreCase(trimmedProvider)
                    && existingPlan.trim().equalsIgnoreCase(trimmedPlan)) {
                throw new IllegalArgumentException(
                        "An insurance network for provider \"" + trimmedProvider + "\" and plan \"" + trimmedPlan
                                + "\" already exists");
            }
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
        // A missing file means "no networks yet", but a file that exists and cannot be read must
        // not be silently treated the same way - that would risk overwriting real network data
        // with nothing on the next save.
        List<String> rawLines;
        try {
            rawLines = TextFileUtil.readLinesChecked(dataFile);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + dataFile.getPath() + ": " + e.getMessage(), e);
        }

        List<String> loadedIds = new ArrayList<>();
        for (String line : rawLines) {
            InsuranceNetwork network = parseLine(line, loadedIds);
            if (network != null) {
                loadedIds.add(network.getNetworkId());
                networks.add(network);
            }
        }
    }

    private InsuranceNetwork parseLine(String line, List<String> loadedIds) {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 6) {
            System.err.println("Skipping malformed insurance network record: " + line);
            return null;
        }
        String networkId = fields[0].trim();
        String providerName = fields[1].trim();
        String planName = fields[2].trim();
        String coverageText = fields[3].trim();
        String activeText = fields[4].trim();
        String managedByStaffId = fields[5].trim();

        if (networkId.isEmpty()) {
            System.err.println("Skipping insurance network with a blank network ID: " + line);
            return null;
        }
        if (loadedIds.contains(networkId)) {
            System.err.println("Skipping duplicate insurance network ID: " + line);
            return null;
        }
        if (providerName.isEmpty()) {
            System.err.println("Skipping insurance network with a blank provider name: " + line);
            return null;
        }
        if (planName.isEmpty()) {
            System.err.println("Skipping insurance network with a blank plan name: " + line);
            return null;
        }
        if (hasLoadedProviderPlanCombination(providerName, planName)) {
            System.err.println("Skipping duplicate insurance network provider/plan combination: " + line);
            return null;
        }

        double coveragePercentage;
        try {
            coveragePercentage = Double.parseDouble(coverageText);
        } catch (NumberFormatException e) {
            System.err.println("Skipping insurance network with a non-numeric coverage percentage \""
                    + coverageText + "\": " + line);
            return null;
        }
        if (Double.isNaN(coveragePercentage) || Double.isInfinite(coveragePercentage)
                || coveragePercentage < 0 || coveragePercentage > 100) {
            System.err.println("Skipping insurance network with an invalid coverage percentage \""
                    + coverageText + "\": " + line);
            return null;
        }

        if (!activeText.equalsIgnoreCase("true") && !activeText.equalsIgnoreCase("false")) {
            System.err.println("Skipping insurance network with an invalid active value \"" + activeText + "\": " + line);
            return null;
        }
        boolean active = Boolean.parseBoolean(activeText);

        AdminStaff managedBy = null;
        if (!managedByStaffId.isEmpty()) {
            User user = userManager.findById(managedByStaffId);
            if (!(user instanceof AdminStaff)) {
                System.err.println("Skipping insurance network with an unknown or non-admin managing staff ID \""
                        + managedByStaffId + "\": " + line);
                return null;
            }
            managedBy = (AdminStaff) user;
        }

        InsuranceNetwork network = new InsuranceNetwork(networkId, providerName, planName, coveragePercentage, active);
        network.setManagedBy(managedBy);
        return network;
    }

    /** Same duplicate rule as create/update, applied to rows already accepted earlier in this load(). */
    private boolean hasLoadedProviderPlanCombination(String providerName, String planName) {
        for (InsuranceNetwork network : networks) {
            if (network.getProviderName().equalsIgnoreCase(providerName)
                    && network.getPlanName().equalsIgnoreCase(planName)) {
                return true;
            }
        }
        return false;
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (InsuranceNetwork network : networks) {
            lines.add(toLine(network));
        }
        try {
            TextFileUtil.writeLinesChecked(dataFile, HEADER, lines);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save insurance network data: " + e.getMessage(), e);
        }
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
