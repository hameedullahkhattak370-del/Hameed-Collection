const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_customer";
const BOB_UID = "bob_customer";
const ADMIN_UID = "admin_hameed";
const ADMIN_EMAIL = "hameedullahkhattak370@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

test("Public: Anyone can view product catalog", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("products").doc("prod1").set({
      title: "Royal Boski Unstitched Suit",
      description: "Premium pure micro-modal blended Boski fabric for luxury wear.",
      category: "Men Unstitched",
      price: 5500,
      salePrice: 4800,
      stock: 30,
      images: ["boski_1.jpg"],
      sizes: ["4.25 Meters"],
      colors: ["Cream White"],
      isFeatured: true,
      isNewArrival: true,
      isArchived: false,
      createdAt: new Date(),
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("products").doc("prod1").get());
  await assertSucceeds(unauthDb.collection("products").get());
});

test("Non-admin: Cannot create or update products", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("products").doc("prod2").set({
      title: "Hacked Suit",
      description: "Description",
      category: "Men",
      price: 100,
      stock: 10,
      createdAt: new Date(),
    })
  );
});

test("Admin: Can create products", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL }).firestore();
  await assertSucceeds(
    adminDb.collection("products").doc("prod2").set({
      title: "Classic Cotton Kurta",
      description: "100% fine Pakistani cotton with subtle embroidery.",
      category: "Kurta Collection",
      price: 3200,
      salePrice: 2800,
      stock: 15,
      images: ["kurta.jpg"],
      sizes: ["S", "M", "L", "XL"],
      colors: ["Navy", "Olive"],
      isFeatured: true,
      isNewArrival: false,
      isArchived: false,
      createdAt: new Date(),
    })
  );
});

test("Customer: Can place a Cash on Delivery order with status Pending", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("orders").doc("order_1").set({
      orderNumber: "HC-20261019-1001",
      customerId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "03001234567",
      customerEmail: "alice@example.com",
      city: "Lahore",
      deliveryAddress: "House 12, Street 4, Gulberg III",
      deliveryNotes: "Ring bell twice",
      items: [
        {
          productId: "prod1",
          title: "Royal Boski Unstitched Suit",
          price: 4800,
          quantity: 1,
          selectedSize: "4.25 Meters",
          selectedColor: "Cream White"
        }
      ],
      subtotal: 4800,
      deliveryFee: 200,
      totalAmount: 5000,
      paymentMethod: "COD",
      status: "Pending",
      createdAt: new Date(),
    })
  );
});

test("Customer: Cannot view another customer's orders", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("orders").doc("order_alice").set({
      orderNumber: "HC-20261019-1002",
      customerId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "03001234567",
      city: "Lahore",
      deliveryAddress: "Gulberg III",
      items: [{ title: "Suit", price: 4000, quantity: 1 }],
      subtotal: 4000,
      deliveryFee: 200,
      totalAmount: 4200,
      paymentMethod: "COD",
      status: "Pending",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("orders").doc("order_alice").get());
});

test("Admin: Can view any customer order and update fulfillment status & tracking", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("orders").doc("order_alice").set({
      orderNumber: "HC-20261019-1002",
      customerId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "03001234567",
      city: "Lahore",
      deliveryAddress: "Gulberg III",
      items: [{ title: "Suit", price: 4000, quantity: 1 }],
      subtotal: 4000,
      deliveryFee: 200,
      totalAmount: 4200,
      paymentMethod: "COD",
      status: "Pending",
      createdAt: new Date(),
    });
  });

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL }).firestore();
  await assertSucceeds(adminDb.collection("orders").doc("order_alice").get());

  await assertSucceeds(
    adminDb.collection("orders").doc("order_alice").update({
      status: "Dispatched",
      courierName: "TCS Express",
      trackingNumber: "TCS9876543210",
      updatedAt: new Date(),
    })
  );
});

test("Guest: Can place a Cash on Delivery order without logging in", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    unauthDb.collection("orders").doc("guest_order_1").set({
      orderNumber: "HC-20261019-9999",
      customerId: "guest_ab12cd34ef56gh78",
      customerName: "Tariq Mehmood",
      customerPhone: "03219876543",
      customerEmail: "",
      city: "Islamabad",
      area: "F-10/2",
      deliveryAddress: "House 55, Street 18",
      deliveryNotes: "Call before arrival",
      items: [
        {
          productId: "prod1",
          title: "Royal Boski Unstitched Suit",
          price: 4800,
          quantity: 1,
          selectedSize: "4.25 Meters",
          selectedColor: "Cream White"
        }
      ],
      subtotal: 4800,
      deliveryFee: 200,
      totalAmount: 5000,
      paymentMethod: "COD",
      paymentStatus: "Unpaid",
      isGuest: true,
      status: "Pending",
      createdAt: new Date(),
    })
  );
});

test("Guest: Cannot read customer orders or list collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("orders").doc("guest_order_1").get());
  await assertFails(unauthDb.collection("orders").get());
});

