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

function pastTimestamp() {
  return new Date(Date.now() - 2000);
}

function validEntry(userId, overrides = {}) {
  const ts = pastTimestamp();
  return {
    userId,
    catalogId: "dune_part_two",
    title: "Dune: Part Two",
    mediaType: "movie",
    status: "want_to_watch",
    priority: true,
    releaseYear: 2024,
    genres: ["Sci-Fi", "Adventure"],
    directorOrCreator: "Denis Villeneuve",
    runtimeOrSeasons: "2h 46m",
    synopsis: "Paul Atreides unites with Chani and the Fremen while seeking revenge.",
    posterKey: "scifi",
    userRating: 0.0,
    reviewTitle: "",
    reviewBody: "",
    favoriteQuote: "",
    containsSpoilers: false,
    rewatchCount: 0,
    episodesWatched: 0,
    totalEpisodes: 0,
    customTitle: false,
    createdAt: ts,
    updatedAt: ts,
    ...overrides,
  };
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

test("Unauthenticated user cannot list or read vaultEntries", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("vaultEntries").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("vaultEntries").doc("dune_part_two").get());
});

test("Authenticated owner can create, list, update review, and delete own vaultEntry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("vaultEntries").doc("dune_part_two");

  await assertSucceeds(docRef.set(validEntry(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("vaultEntries").get());

  await assertSucceeds(
    docRef.update({
      status: "watched",
      userRating: 9.5,
      reviewTitle: "Monumental sound and scale",
      reviewBody: "A towering theatrical experience.",
      favoriteQuote: "May thy knife chip and shatter.",
      containsSpoilers: false,
      updatedAt: pastTimestamp(),
    })
  );

  await assertSucceeds(docRef.delete());
});

test("Cross-user read, create, update, and delete are denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDoc = aliceDb.collection("users").doc(ALICE_UID).collection("vaultEntries").doc("dune_part_two");
  await assertSucceeds(aliceDoc.set(validEntry(ALICE_UID)));

  const bobRefToAlice = bobDb.collection("users").doc(ALICE_UID).collection("vaultEntries").doc("dune_part_two");
  await assertFails(bobRefToAlice.get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("vaultEntries").get());
  await assertFails(bobRefToAlice.update({ status: "watched", updatedAt: pastTimestamp() }));
  await assertFails(bobRefToAlice.delete());

  // Spoofed userId inside Bob's own collection is also denied
  const bobSpoofDoc = bobDb.collection("users").doc(BOB_UID).collection("vaultEntries").doc("spoof_item");
  await assertFails(bobSpoofDoc.set(validEntry(ALICE_UID)));
});

test("Rejects ghost fields, invalid rating bounds, invalid doc ID, and immutable field mutation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("vaultEntries").doc("dune_part_two")
      .set(validEntry(ALICE_UID, { isVerified: true }))
  );

  // Out-of-bounds rating
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("vaultEntries").doc("dune_part_two")
      .set(validEntry(ALICE_UID, { userRating: 11.0 }))
  );

  // Invalid doc ID
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("vaultEntries").doc("bad id!")
      .set(validEntry(ALICE_UID))
  );

  // Valid create, then attempt to mutate immutable catalogId
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("vaultEntries").doc("dune_part_two");
  await assertSucceeds(docRef.set(validEntry(ALICE_UID)));
  await assertFails(docRef.update({ catalogId: "hacked_id", updatedAt: pastTimestamp() }));
});
