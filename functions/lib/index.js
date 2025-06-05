"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.onNoteCreated = exports.onListShared = void 0;
const firestore_1 = require("firebase-functions/v2/firestore");
const admin = __importStar(require("firebase-admin"));
admin.initializeApp();
exports.onListShared = (0, firestore_1.onDocumentUpdated)("users/{userId}/noteslist/{listId}", async (event) => {
    var _a, _b;
    console.log("¡Lista compartida!", event.params);
    const before = (_a = event.data) === null || _a === void 0 ? void 0 : _a.before.data();
    const after = (_b = event.data) === null || _b === void 0 ? void 0 : _b.after.data();
    const oldContributors = (before === null || before === void 0 ? void 0 : before.contributors) || [];
    const newContributors = (after === null || after === void 0 ? void 0 : after.contributors) || [];
    const creator = after === null || after === void 0 ? void 0 : after.creator;
    const listName = after === null || after === void 0 ? void 0 : after.name;
    const addedContributors = newContributors.filter((contributor) => !oldContributors.includes(contributor));
    for (const contributorId of addedContributors) {
        const tokensSnapshot = await admin
            .firestore()
            .collection("users")
            .doc(contributorId)
            .collection("fcm_tokens")
            .get();
        if (!tokensSnapshot.empty) {
            const token = tokensSnapshot.docs[0].data().token;
            const message = {
                notification: {
                    title: "Nueva lista disponible",
                    body: `El usuario ${creator} te ha compartido la lista "${listName}".`,
                },
                android: {
                    notification: {
                        icon: "ic_notification",
                    },
                },
                token: token,
            };
            await admin.messaging().send(message);
        }
    }
});
exports.onNoteCreated = (0, firestore_1.onDocumentCreated)("users/{userId}/noteslist/{listId}/notes/{noteId}", async (event) => {
    var _a;
    console.log("¡Nota creada!", event.params);
    const noteData = (_a = event.data) === null || _a === void 0 ? void 0 : _a.data();
    const userId = event.params.userId;
    const tokensSnapshot = await admin
        .firestore()
        .collection("users")
        .doc(userId)
        .collection("fcm_tokens")
        .get();
    if (!tokensSnapshot.empty) {
        const token = tokensSnapshot.docs[0].data().token;
        const message = {
            notification: {
                title: "Nueva nota creada",
                body: `Has creado: ${(noteData === null || noteData === void 0 ? void 0 : noteData.title) || "Sin título"}`,
            },
            android: {
                notification: {
                    icon: "ic_notification",
                },
            },
            token: token,
        };
        await admin.messaging().send(message);
    }
});
//# sourceMappingURL=index.js.map