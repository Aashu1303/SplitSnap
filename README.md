# SplitSnap - Android App

A Jetpack Compose Android application for capturing and splitting receipt expenses among friends.

## Architecture

This app follows the **MVVM (Model-View-ViewModel)** architecture pattern:

```
android/app/src/main/java/com/splitsnap/
├── data/
│   ├── local/
│   │   ├── dao/          # Room Database DAOs
│   │   ├── entity/       # Room Database Entities
│   │   └── database/     # Room Database Configuration
│   └── repository/       # Data Repository
├── domain/
│   └── model/            # Domain Models
├── ui/
│   ├── components/       # Reusable Composables
│   ├── navigation/       # Navigation Graph
│   ├── screens/          # Screen Composables
│   │   ├── home/
│   │   ├── camera/
│   │   ├── editreceipt/
│   │   └── splitsummary/
│   └── theme/            # Material 3 Theme
├── viewmodel/            # ViewModels
├── MainActivity.kt
└── SplitSnapApplication.kt
```

## Features

- **Home Screen**: View all receipts with status indicators (Draft, Split, Completed)
- **Camera Screen**: Simulate capturing receipts (ready for real camera integration)
- **Edit Receipt**: 
  - Add/remove participants
  - Assign items to people with quantity controls
  - Multi-select items for batch assignment
  - Expandable item details
- **Split Summary**: View individual splits with itemized breakdown

## Tech Stack

- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with StateFlow
- **Database**: Room for local storage
- **Navigation**: Compose Navigation
- **Serialization**: Gson for JSON handling
- **Async**: Kotlin Coroutines & Flow

## Building the Project

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `android` folder and select it
4. Wait for Gradle sync to complete
5. Run the app on an emulator or physical device (API 26+)

### Build Commands

```bash
# Navigate to android directory
cd android

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

## Data Models

### Receipt
- `id`: Unique identifier
- `storeName`: Name of the store
- `date`: Date of purchase
- `total`: Total amount in cents
- `status`: Draft, Split, or Completed

### ReceiptItem
- `id`: Unique identifier
- `receiptId`: Reference to parent receipt
- `name`: Item name
- `quantity`: Item quantity
- `price`: Unit price in cents
- `assignments`: JSON map of personId → quantity

### Person
- `id`: Unique identifier
- `name`: Person's name
- `initial`: First letter of name
- `avatarColor`: Color identifier for avatar
- `isMe`: Boolean flag for the owner
- `relationship`: Optional relationship label

## Theme

The app uses a custom Material 3 theme with a teal-blue color palette:

- **Primary**: #14B8A6 (Teal)
- **Secondary**: #0EA5E9 (Sky Blue)
- **Background**: #F0FDFA (Light Mint)

## Future Enhancements

1. **Real Camera Integration**: Replace simulated capture with CameraX
2. **OCR Processing**: Add ML Kit for receipt text extraction
3. **Share Functionality**: Share split summaries via messaging apps
4. **Payment Integration**: Track payment status per person
5. **Receipt History**: Archive and search past receipts
6. **Export Options**: Export to PDF or spreadsheet
