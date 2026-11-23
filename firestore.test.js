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

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp(offsetMs = 1000) {
  return new Date(Date.now() - offsetMs);
}

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

test("Unauthenticated user: cannot read or write customers or appointments", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = pastTimestamp(2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("customers").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("appointments").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_1").set({
      userId: ALICE_UID,
      name: "Unauth Client",
      phoneNo: "555-123-4567",
      email: "unauth@example.com",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("appointments").doc("apt_1").set({
      userId: ALICE_UID,
      customerName: "Unauth Client",
      serviceName: "Haircut",
      appointmentDate: "2026-10-28",
      appointmentTime: "11:00",
      status: "Pending",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Authenticated user: cannot read, create, update, or delete another user's data", async () => {
  const ts = pastTimestamp(10000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).collection("customers").doc("cust_1").set({
      userId: BOB_UID,
      name: "Bob Client",
      phoneNo: "555-123-4567",
      email: "bobclient@example.com",
      createdAt: ts,
      updatedAt: ts,
    });
    await db.collection("users").doc(BOB_UID).collection("appointments").doc("apt_1").set({
      userId: BOB_UID,
      customerName: "Bob Client",
      serviceName: "Signature Cut",
      appointmentDate: "2026-10-28",
      appointmentTime: "11:00",
      status: "Confirmed",
      createdAt: ts,
      updatedAt: ts,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("customers").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("customers").doc("cust_1").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("customers").doc("cust_1").update({
    name: "Hijacked",
    updatedAt: pastTimestamp(1000),
  }));
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("customers").doc("cust_1").delete());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("customers").doc("cust_2").set({
    userId: BOB_UID,
    name: "Injected",
    phoneNo: "555-999-0000",
    email: "injected@example.com",
    createdAt: pastTimestamp(1000),
    updatedAt: pastTimestamp(1000),
  }));

  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("appointments").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("appointments").doc("apt_1").update({
    status: "Cancelled",
    updatedAt: pastTimestamp(1000),
  }));
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("appointments").doc("apt_1").delete());
});

test("Authenticated user: Kotlin-mirrored customer create, list, partial update, and delete succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const customerRef = aliceDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_alice_1");
  const created = pastTimestamp(10000);

  // Full Kotlin Customer.toCreateMap payload (no @DocumentId `id` field)
  await assertSucceeds(customerRef.set({
    userId: ALICE_UID,
    name: "Emma Watson",
    phoneNo: "+1-555-0199",
    dob: "1992-04-15",
    email: "emma.watson@example.com",
    address: "742 Evergreen Terrace",
    preferredStylist: "Marcus Bell",
    notes: "Prefers organic shampoo",
    totalVisits: 2,
    createdAt: created,
    updatedAt: created,
  }));

  // Minimal Customer.toCreateMap payload (optional blank fields omitted)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_alice_min").set({
      userId: ALICE_UID,
      name: "Minimal Client",
      phoneNo: "555-0100",
      email: "min@example.com",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Read & List
  await assertSucceeds(customerRef.get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("customers").get());

  // Partial update mirroring Customer.toUpdateMap()
  await assertSucceeds(customerRef.update({
    name: "Emma Watson-Smith",
    phoneNo: "+1-555-0199",
    email: "emma.watson@example.com",
    totalVisits: 3,
    updatedAt: pastTimestamp(1000),
  }));

  // Delete
  await assertSucceeds(customerRef.delete());
});

test("Authenticated user: Kotlin-mirrored appointment create, status update, and delete succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const aptRef = aliceDb.collection("users").doc(ALICE_UID).collection("appointments").doc("apt_1");
  const created = pastTimestamp(10000);

  await assertSucceeds(aptRef.set({
    userId: ALICE_UID,
    customerId: "cust_alice_1",
    customerName: "Emma Watson",
    customerPhone: "+1-555-0199",
    serviceName: "Balayage Hair Color",
    price: 180,
    stylist: "Marcus Bell",
    appointmentDate: "2026-10-28",
    appointmentTime: "11:00",
    status: "Confirmed",
    notes: "Requires strand test first",
    createdAt: created,
    updatedAt: created,
  }));

  await assertSucceeds(aptRef.get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("appointments").get());

  // Mirror SalonRepository.updateAppointmentStatus
  await assertSucceeds(aptRef.update({
    status: "Completed",
    updatedAt: pastTimestamp(1000),
  }));

  await assertSucceeds(aptRef.delete());
});

test("Security validation: rejects @DocumentId 'id' field, immutable field mutations, future timestamps, and invalid enums", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = pastTimestamp(10000);

  // Reject extra `id` field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_bad_id").set({
      id: "cust_bad_id",
      userId: ALICE_UID,
      name: "Bad ID Client",
      phoneNo: "555-0199",
      email: "bad@example.com",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject future tampered timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_future").set({
      userId: ALICE_UID,
      name: "Future Client",
      phoneNo: "555-0199",
      email: "future@example.com",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Reject invalid status enum on appointment
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("appointments").doc("apt_bad_status").set({
      userId: ALICE_UID,
      customerName: "Emma Watson",
      serviceName: "Balayage",
      appointmentDate: "2026-10-28",
      appointmentTime: "11:00",
      status: "INVALID_STATUS",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Create valid customer then attempt immutable createdAt mutation
  const custRef = aliceDb.collection("users").doc(ALICE_UID).collection("customers").doc("cust_immut");
  await assertSucceeds(custRef.set({
    userId: ALICE_UID,
    name: "Immutable Check",
    phoneNo: "555-0199",
    email: "immut@example.com",
    createdAt: created,
    updatedAt: created,
  }));

  await assertFails(custRef.update({
    createdAt: pastTimestamp(500),
    updatedAt: pastTimestamp(500),
  }));
});
