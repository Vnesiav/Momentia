Deskripsi singkat tentang aplikasi:
MOMENTIA merupakan aplikasi sosial media minimalis yang dapat digunakan untuk bersosialisasi dengan orang lain. Aplikasi sosial media semakin menjadi bagian tak terpisahkan dari kehidupan sehari-hari, 
di mana berbagi momen dengan temanteman telah menjadi kebiasaan umum. MOMENTIA hadir untuk memberikan pengalaman berbagi foto yang lebih personal dengan menambahkan informasi kontekstual seperti waktu 
pengambilan foto dan lokasi. Dengan memanfaatkan fitur-fitur native seperti kamera, lokasi, dan galeri, MOMENTIA memungkinkan pengguna untuk tidak hanya mengabadikan momen, tetapi juga mengirimkannya 
kepada teman dengan detail yang memberikan cerita lebih dalam. Selain itu, aspek privasi dan keamanan dalam menambahkan teman menjadi faktor penting yang dipertimbangkan dalam pengembangan aplikasi ini.

Kelompok 2:
1. Angelima Khosina - 00000067456
2. Ryu Ivan Wijaya - 00000065448
3. Vianca Vanesia Barhan - 00000065031
4. Walter Adrian Samuel - 00000067030

Fitur-fitur yang sudah diaplikasikan:
1. Implementasi proses CRUD (tabel users)
   - Create (create account, create friend request, create friend, create storage for image)
   - Read (read account, read friend request, read friend)
   - Update (update profile picture, update name, update password, update number)
   - Delete (delete account, delete friend request)
2. Implementasi authentication (login, register)
3. Implementasi native feature (camera, gallery, internet access, local storage)

Fitur/page yang dihapus:
1. OTP authentication, karena fiturnya berbayar
2. Change email address, karena firebase tidak mengizinkan

Fitur yang ditambahkan:
1. Fitur change dan forgot password
3. Fitur friend request

Struktur tabel:
users (collection)
  └── {userId} (document)
      ├── username: string
      ├── email: string
      ├── phoneNumber: string
      ├── firstName: string
      ├── lastName: string
      ├── avatarUrl: string (link to Firebase Storage)
      ├── friends: array of userId (references to friends)
      ├── snapsReceived: array of {snapId}
      ├── snapsSent: array of {snapId}
      ├── stories: array of {storyId}
      ├── createdAt: timestamp
      
chats (collection)
  └── {chatId} (document)
      ├── users: array of userId (2 userIds for the participants)
      └── messages (subcollection)
          └── {messageId} (document)
              ├── senderId: string (userId of sender)
              ├── messageText: string
              ├── mediaUrl: string (optional, link to photo/video in Storage)
              ├── sentAt: timestamp

memories (collection)
  └── {memoryId} (document)
      ├── location: GeoPoint (latitude, longitude)
      ├── mediaUrl: string (link to photo/video in Firebase Storage)
      ├── receiverId: string (userId of the receiver)
      ├── senderId: string (userId of the sender)
      ├── sentAt: timestamp
      ├── viewed: boolean (true if the memory has been viewed)
