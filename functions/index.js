const functions = require('firebase-functions');
const admin     = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

// Sempre que uma nova mensagem for criada em groups/{groupId}/messages/{messageId}
exports.onNewGroupMessage = functions.firestore
  .document('groups/{groupId}/messages/{messageId}')
  .onCreate(async (snap, context) => {
    const message = snap.data();
    const groupId = context.params.groupId;
    const authorDeviceId = message.senderId;        // usa senderId como no teu AlarmMessageInfo

    // 1) Busca o documento do grupo para pegar o mapa de membros
    const groupSnap = await db.collection('groups').doc(groupId).get();
    if (!groupSnap.exists) return null;
    const members = groupSnap.get('members') || {}; // é Map<deviceId, userName>

    // 2) Reúne os tokens de todos os membros, menos o autor
    const tokens = [];
    for (const [deviceId, userName] of Object.entries(members)) {
      if (deviceId === authorDeviceId) continue;
      const devSnap = await db.collection('devices').doc(deviceId).get();
      const token = devSnap.get('fcm_token');
      if (token) tokens.push(token);
    }
    if (tokens.length === 0) return null;

    // 3) Monta o payload usando os campos do teu modelo
    const payload = {
      notification: {
        title: `${message.senderName} em ${groupSnap.get('groupName')}`,
        body: message.text
      },
      data: {
        groupId:     groupId,
        senderId:    message.senderId,
        senderName:  message.senderName,
        messageText: message.text
      }
    };

    // 4) Envia para todos os tokens coletados
    await admin.messaging().sendToDevice(tokens, payload);
    return null;
  });

