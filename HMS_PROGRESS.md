# HMS-OODJ Progress Summary

Continuation notes for the next working session. This is a Java Swing / plain-text-file
Hospital Management System coursework project (no database, no frameworks). Everything
below reflects the actual state of the codebase, not the original plan.

## 1. Current completed features

### Authentication & navigation
- `LoginFrame` (bare window, login form only) → on success → `MainFrame` (header/sidebar/
  footer shell with a live clock) showing the correct role dashboard. No application
  chrome exists before login.
- Role-based routing via `SessionManager.hasRole(...)`, no re-authentication in the GUI.
- Logout disposes `MainFrame` and returns to a fresh `LoginFrame`.
- Username validation: 4-20 chars, letters/digits/underscore, must contain a letter, no
  spaces. Email/phone format validation. All in `ValidationUtil`, called from `UserManager`.

### Admin (fully built, no placeholders left)
- **Edit My Profile**: phone/email edit + separate Change Password dialog.
- **Manage Users**: register (all 4 roles), edit profile (phone/email only), reset
  password, toggle active/inactive, delete, search.
- **Manage Doctors**: view/search, view details, assign/reassign/unassign Medical
  Manager, update specialization (controlled dropdown list).
- **View Appointments**: read-only list + search across all appointments.
- **Manage Hospital Assets**: CRUD + toggle availability (types: CONSULTATION_ROOM,
  WARD, LAB, IMAGING_ROOM).
- **Manage Consultation Rates**: CRUD + toggle active.
- **Manage Insurance Networks**: CRUD + toggle active (coverage % validated 0-100).

### Medical Manager (fully built, no placeholders left)
- **Edit My Profile**: phone/email edit + separate Change Password dialog. User ID/
  username/role shown but never editable.
- **Manage Clinical Departments**: CRUD + toggle active + search; duplicate name
  rejected (case-insensitive), on create and on rename.
- **Manage Assessment Types**: CRUD + toggle active; duplicate name rejected; category
  restricted to LAB/IMAGING/VITALS/PHYSICAL_EXAM; grading rule restricted to
  NUMERIC_RANGE/PASS_FAIL/DESCRIPTIVE; charge non-negative.
- **Manage Doctor Rosters/Shifts**: create/edit/remove shifts for the manager's own
  supervised doctors only; start<end; no overlapping shifts per doctor per day.
- **View Reports**: Hospital Metrics report and Revenue report, via `ReportManager`
  only (never touches the concrete report classes) — this is the polymorphism example.

### Doctor (fully built, no placeholders left)
- **Edit My Profile**: phone/email edit + separate Change Password dialog.
- **View My Appointments**: read-only list.
- **Record Assessment Result**: pick one of own appointments, pick an *active*
  `AssessmentType` (Medical-Manager-curated), enter a value validated against that
  type's grading rule, optional grade/interpretation. Opens/reuses the appointment's
  medical record automatically (`ClinicalManager.getOrOpenRecord`, idempotent).
- **Log Vitals & Consultation Notes**: select appointment, fill vitals/notes/diagnosis,
  save. Loads existing record values when switching appointments. Separate "Finalize
  Record" button that saves current fields then calls `finalizeRecord()` (requires
  non-empty consultation notes), marking appointment COMPLETED.
- **Issue Prescriptions**: select appointment, fill medication/dosage/instructions,
  issue. Shows existing prescriptions for the selected appointment's record.
- **Request Lab / Imaging**: select appointment, choose LAB or IMAGING, submit.
  Shows existing requests for the selected appointment's record.

### Patient (fully built, no placeholders left)
- **Edit My Profile**: phone/email edit + separate Change Password dialog.
- **Book Appointment**: browse active doctors, see the selected doctor's configured
  shifts (informational table), book by date/time. Booking is rejected outside any
  shift, at/after shift end, on double-booked slots, or on days the doctor has no shift.
- **View Medical History**: table of the patient's COMPLETED appointment records;
  selecting a row expands full details (vitals, diagnosis, notes, assessment results)
  in a detail panel below.
- **View Prescriptions & Bills**: two sections — prescriptions from all completed
  records (medication, dosage, instructions, doctor, date) and payment records
  (payment ID, appointment, amount, status, date). Read-only.
- **Submit Feedback**: dropdown of COMPLETED appointments, rating spinner (1–5),
  comment text area. ClinicalManager enforces the one-feedback-per-doctor rule.

### Cross-cutting
- All appointment booking/rescheduling requires the slot to fall inside a
  `DoctorShift` (checked in `AppointmentManager`).
- All assessment results reference an `AssessmentType`; the type's `validateValue()`
  method is the single source of truth for value-format validation.

## 2. Files created/modified (cumulative)

80 `.java` files under `src/hms`. Key groupings:

**Models** (`src/hms/model`): original UML set (`User` + 4 subclasses, `Appointment`,
`MedicalRecord`, `Prescription`, `MedicalRequest`, `Feedback`, `Payment`,
`DoctorAssignment`, `ClinicalDepartment`, `HospitalAsset`, `ConsultationRate`,
`InsuranceNetwork`, `HospitalReport`/`HospitalMetricsReport`/`RevenueReport`,
`AssessmentResult`) plus **new**: `AssessmentType`, `DoctorShift`.

**Managers** (`src/hms/manager`, 12 total): `UserManager`, `SessionManager`,
`AppointmentManager`, `ClinicalManager`, `BillingManager`, `ReportManager`,
`HospitalAssetManager`, `ConsultationRateManager`, `InsuranceNetworkManager`,
`AssessmentTypeManager`, `DoctorShiftManager`, `ClinicalDepartmentManager`.

**GUI** (`src/hms/ui`):
- `common`: `LoginFrame`, `LoginPanel`, `MainFrame`, `AbstractDashboardPanel`,
  `AppointmentTableUtil`, `Theme`, `UIStyles`.
- `admin` (8 panels): `AdminDashboardPanel`, `EditProfilePanel`,
  `UserManagementPanel`, `DoctorManagementPanel`, `AppointmentManagementPanel`,
  `HospitalAssetManagementPanel`, `ConsultationRateManagementPanel`,
  `InsuranceNetworkManagementPanel`.
- `medicalmanager` (6 panels): `MedicalManagerDashboardPanel`, `EditProfilePanel`,
  `ClinicalDepartmentManagementPanel`, `AssessmentTypeManagementPanel`,
  `DoctorShiftManagementPanel`, `ReportViewPanel`.
- `doctor` (7 panels): `DoctorDashboardPanel`, `EditProfilePanel`,
  `MyAppointmentsPanel`, `RecordAssessmentPanel`, `LogVitalsPanel`,
  `IssuePrescriptionPanel`, `RequestLabImagingPanel`.
- `patient` (7 panels): `PatientDashboardPanel`, `EditProfilePanel`,
  `AppointmentBookingPanel`, `ViewMedicalHistoryPanel`,
  `ViewPrescriptionsBillsPanel`, `SubmitFeedbackPanel`.

**Utilities**: `hms.util.ValidationUtil`, `hms.util.IdGenerator`,
`hms.data.TextFileUtil`.

**Data files** (`data/*.txt`, 14 total): all pipe-delimited with `#`-prefixed header.

## 3. Current architecture

Strict layering, unchanged:

```
GUI (Swing panels)
    -> Manager classes (business rules, one manager per file/entity family)
        -> Model classes (private fields, plain getters/setters, a few
           "controlled behaviour" methods e.g. AssessmentType.validateValue())
            -> Storage (TextFileUtil + hand-rolled pipe-delimited parsing,
               one manager owns one or more data files)
```

Manager dependency graph (construction order in `Main.java`):
```
UserManager
  -> DoctorShiftManager(UserManager)
    -> AppointmentManager(UserManager, DoctorShiftManager)
  -> AssessmentTypeManager(UserManager)
    -> ClinicalManager(UserManager, AppointmentManager, AssessmentTypeManager)
  -> BillingManager(AppointmentManager)
  -> ReportManager()                         [no persistence of its own]
  -> HospitalAssetManager(UserManager)
  -> ConsultationRateManager(UserManager)
  -> InsuranceNetworkManager(UserManager)
  -> ClinicalDepartmentManager(UserManager)
  -> SessionManager(UserManager)
```

GUI wiring pattern: `Main.java` constructs every manager once and threads them down
through `showLoginFrame` → `showMainFrame` → `createDashboardPanel` → each role
dashboard → each feature panel. Every panel that has a "Back to Dashboard" button
carries the *full* set of that role's managers purely to reconstruct its dashboard.

**Doctor dashboard** now takes `(SessionManager, UserManager, AppointmentManager,
ClinicalManager, AssessmentTypeManager, MainFrame, Runnable)` — `UserManager` was added
this session so all doctor panels can navigate back to the dashboard and support Edit Profile.

**Patient dashboard** now takes `(SessionManager, UserManager, AppointmentManager,
DoctorShiftManager, ClinicalManager, BillingManager, MainFrame, Runnable)` — `ClinicalManager`
and `BillingManager` were added this session to support the new patient panels.

OOP demonstrations still intact and unmodified:
- **Inheritance**: `User` (abstract) → `AdminStaff`/`MedicalManager`/`Doctor`/`Patient`.
- **Abstraction**: `User.getUserType()` abstract; `HospitalReport` interface.
- **Polymorphism**: `ReportManager.generate(HospitalReport)` works identically for
  `HospitalMetricsReport` and `RevenueReport`.
- **Encapsulation**: all model fields private; validation lives in `ValidationUtil`,
  managers, or model "controlled behaviour" methods — never in the GUI layer.

## 4. Remaining tasks (not started / known gaps)

1. **AssessmentType charge is not connected to billing** — `AssessmentType.baseCharge`
   and `ConsultationRate` are never read by `BillingManager`; billing is currently a
   flat manually-entered amount. No Admin "record payment" screen exists either.
2. **ClinicalDepartment is not linked to Doctor or HospitalAsset** — a doctor's
   specialty is still a free-text-from-fixed-list string on `Doctor`, not a reference
   to a `ClinicalDepartment`; hospital assets aren't associated with a department either.
3. **Medical Manager "View Reports"** only has the original two report types
   (Metrics, Revenue) — nothing surfaces assessment-type usage or shift/roster data.
4. **No date-range filtering** on either report — they always aggregate all-time data.
5. **Constructor parameter growth** — DoctorDashboard takes 7 params, PatientDashboard
   takes 8, Main.createDashboardPanel takes 14. Not broken, consistent with decisions.
6. Passwords remain **plaintext** in `users.txt` — acceptable for coursework scope
   but worth noting in the report's Limitations section.

## 5. Important decisions already made

- **Text-file schema evolution is append-only.** Every added column goes at the end;
  loaders treat short rows as "column absent, use safe default".
- **IDs are short sequential prefixed strings** (`PT001`, `DR002`, `AT001`, `SH003`),
  generated by `IdGenerator.next(prefix, existingIds)`.
- **No bundling/context object** to reduce constructor parameter counts — deliberately
  avoided to keep the architecture simple for a lab project.
- **AssessmentType is the single source of truth for result validation.**
- **DoctorShift has no "active" flag.** Shifts are either present or deleted.
- **A Medical Manager may only create/edit shifts for doctors assigned to them.**
- **Getting/opening a medical record is idempotent** via `ClinicalManager.getOrOpenRecord()`.
- **Search features are pure GUI-side filtering** over an already-fetched manager list.
- **LogVitalsPanel saves current field values before finalizing**, so the doctor's
  unsaved input is not lost when clicking Finalize.

## 6. Testing results

All prior test suites passed (274/274 total). New panels added this session
have not yet had a dedicated backend test run — all manager-layer logic they use was
already covered by previous test suites. GUI smoke-testing should be done by launching
the app and exercising each new panel.

**Not yet tested at all:** anything listed under "Remaining tasks" above.
