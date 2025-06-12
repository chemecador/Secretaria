import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";

admin.initializeApp();

export const onListShared = onDocumentUpdated(
  "users/{userId}/noteslist/{listId}",
  async (event) => {
    console.log("¡Lista compartida!", event.params);
    const before = event.data?.before.data();
    const after = event.data?.after.data();

    const oldContributors = before?.contributors || [];
    const newContributors = after?.contributors || [];
    const creator = after?.creator;
    const listName = after?.name;

    const addedContributors = newContributors.filter(
      (contributor: string) => !oldContributors.includes(contributor)
    );

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
  }
);
