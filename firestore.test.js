const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any user data", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("goals").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("sessions").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("dayStatuses").get()
  );
});

test("UserProfile: owner create & partial update succeed; cross-user & ghost fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Dev",
      dailyTargetMinutes: 360,
      streakFreezes: 2,
      shieldedApps: ["Instagram", "YouTube"],
      darkModeEnabled: true,
      hapticsEnabled: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Partial update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      dailyTargetMinutes: 420,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user read/write denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      dailyTargetMinutes: 60,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt change and ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Goals: Kotlin-mirrored create, update, list, and security bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  // Valid create (no 'id' field since Kotlin uses @DocumentId)
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("goals")
      .doc("goal_1")
      .set({
        userId: ALICE_UID,
        dateKey: "2026-11-22",
        title: "Complete System Architecture Design",
        targetMinutes: 120,
        category: "Design",
        tier: "ESSENTIAL",
        completed: false,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Valid partial toggle update
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("goals")
      .doc("goal_1")
      .update({
        completed: true,
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Owner list succeeds; cross-user list fails
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("goals").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("goals").get()
  );

  // Reject extra 'id' field, invalid tier, and future timestamp
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("goals")
      .doc("goal_bad_id")
      .set({
        id: "goal_bad_id",
        userId: ALICE_UID,
        dateKey: "2026-11-22",
        title: "Invalid",
        targetMinutes: 60,
        category: "Code",
        tier: "ESSENTIAL",
        completed: false,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("goals")
      .doc("goal_bad_tier")
      .set({
        userId: ALICE_UID,
        dateKey: "2026-11-22",
        title: "Invalid tier",
        targetMinutes: 60,
        category: "Code",
        tier: "SUPER",
        completed: false,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("goals")
      .doc("goal_future")
      .set({
        userId: ALICE_UID,
        dateKey: "2026-11-22",
        title: "Future timestamp",
        targetMinutes: 60,
        category: "Code",
        tier: "OPTIONAL",
        completed: false,
        createdAt: new Date(Date.now() + 60000),
        updatedAt: ts,
      })
  );
});

test("Sessions: Kotlin-mirrored create (with and without goalId), update subNotes, list", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  // Create without optional goalId (omitted via filterValues)
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("sessions")
      .doc("sess_1")
      .set({
        userId: ALICE_UID,
        dateKey: "2026-11-22",
        title: "Deep Work Architecture",
        category: "Code",
        startMinuteOfDay: 540,
        durationMinutes: 90,
        tags: ["#kotlin", "#compose"],
        subNotes: "- Implemented focus dial\n- Verified rules",
        shieldActive: true,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Update subNotes
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("sessions")
      .doc("sess_1")
      .update({
        subNotes: "- Updated deliverables",
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Create with valid goalId
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("sessions")
      .doc("sess_2")
      .set({
        userId: ALICE_UID,
        dateKey: "2026-11-22",
        title: "Algorithms Review",
        category: "Study",
        startMinuteOfDay: 720,
        durationMinutes: 45,
        tags: ["#algorithms"],
        subNotes: "Chapter 4 notes",
        goalId: "goal_1",
        shieldActive: false,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("sessions").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("sessions").get()
  );

  // Reject explicit null goalId
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("sessions")
      .doc("sess_null")
      .set({
        userId: ALICE_UID,
        dateKey: "2026-11-22",
        title: "Null goalId",
        category: "Study",
        startMinuteOfDay: 720,
        durationMinutes: 45,
        tags: [],
        subNotes: "",
        goalId: null,
        shieldActive: false,
        createdAt: ts,
        updatedAt: ts,
      })
  );
});

test("DayStatuses: Streak Freeze record validation and ownership", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("dayStatuses")
      .doc("2026-11-21")
      .set({
        userId: ALICE_UID,
        dateKey: "2026-11-21",
        freezeUsed: true,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("dayStatuses")
      .doc("invalid-date")
      .set({
        userId: ALICE_UID,
        dateKey: "invalid-date",
        freezeUsed: true,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("dayStatuses")
      .doc("2026-11-21")
      .get()
  );
});

test("TaskLogs: Kotlin-mirrored create (with and without durationMinutes), list, delete, and bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = new Date(Date.now() - 5000);

  // Unauthenticated access denied
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("taskLogs").get()
  );

  // Create without optional durationMinutes (omitted via filterValues)
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("taskLogs")
      .doc("log_1")
      .set({
        userId: ALICE_UID,
        workDone: "Implemented Firebase task log repository and rules",
        category: "Code",
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Create with optional durationMinutes
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("taskLogs")
      .doc("log_2")
      .set({
        userId: ALICE_UID,
        workDone: "Reviewed distributed systems lecture notes",
        category: "Study",
        durationMinutes: 45,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Ordered query matching Kotlin repository (.orderBy("createdAt", DESCENDING))
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("taskLogs")
      .orderBy("createdAt", "desc")
      .get()
  );

  // Cross-user read/write/delete denied
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("taskLogs").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("taskLogs")
      .doc("log_1")
      .delete()
  );

  // Reject empty workDone, explicit null durationMinutes, extra 'id', and oversized workDone
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("taskLogs")
      .doc("log_empty")
      .set({
        userId: ALICE_UID,
        workDone: "",
        category: "Code",
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("taskLogs")
      .doc("log_null_dur")
      .set({
        userId: ALICE_UID,
        workDone: "Test explicit null",
        category: "Code",
        durationMinutes: null,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("taskLogs")
      .doc("log_bad_id")
      .set({
        id: "log_bad_id",
        userId: ALICE_UID,
        workDone: "Extra id key",
        category: "Code",
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("taskLogs")
      .doc("log_1")
      .delete()
  );
});
