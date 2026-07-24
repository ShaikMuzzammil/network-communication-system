# Python GUI Client - Interface & Screen Documentation

## Overview
This document provides comprehensive screen descriptions for the SocketComm Python GUI Client applications built with CustomTkinter, featuring professional dark-theme interfaces for both Server Control Center and Client Workstation.

## Server Control Center Screenshots

### 1. Initial Launch State
**File:** `server_initial.png`  
**Window Title:** "SocketComm — Server Control Center"  
**Dimensions:** 420×680px (minimum 360×500px)

**Visual Elements:**
- **Header Section (62px height):**
  - Circular avatar (42px) with blue background (#2B5278), letter "S"
  - Title: "Server Admin" in white bold (Segoe UI, 13pt)
  - Status: "● waiting for client" in yellow (#FEE75C)
- **Divider Line:** 1px dark line (#0D1117)
- **Chat Area:** Dark background (#0E1621) occupying remaining space
  - System message: "Server ready on 127.0.0.1:12345" in muted blue-gray
- **Input Area (62px height):**
  - Text entry field: rounded, placeholder "Write a message…"
  - Send button: circular, blue accent, **disabled state** (grayed out)

### 2. Client Connected State
**File:** `server_client_connected.png`

**State Changes from Initial:**
- Status text changes to: "● online · 192.168.1.x" in green (#51CF66)
- Send button becomes **enabled** (blue background #2B5278, hover #3A6B9F)
- System message appears: "Client connected from 192.168.1.x:port"
- Input field becomes interactive

### 3. Active Conversation - Outgoing Message
**File:** `server_outgoing_msg.png`

**Message Bubble Appearance:**
- Background: Blue (#2B5278)
- Position: Right-aligned
- Corner radius: 12px
- Text color: White (#FFFFFF)
- Wrap length: 230px
- Timestamp: Bottom-right, small (8pt), secondary color (#7BA7C7)
- Format: "HH:mm"

### 4. Active Conversation - Incoming Message
**File:** `server_incoming_msg.png`

**Message Bubble Appearance:**
- Background: Dark slate (#182533)
- Position: Left-aligned
- Corner radius: 12px
- Text color: Light gray (#EAECEE)
- Wrap length: 230px
- Timestamp: Bottom-right, tertiary color (#7A8896)

### 5. Client Disconnected State
**File:** `server_disconnected.png`

**State Changes:**
- Status reverts to: "● waiting for client" in yellow
- Send button returns to **disabled** state
- System message: "Client disconnected · waiting for next connection…"
- Previous conversation history preserved in chat area
- Server automatically resumes accepting new connections

## Client Workstation Screenshots

### 1. Offline/Initial State
**File:** `client_offline.png`  
**Window Title:** "SocketComm — Workstation"  
**Color Scheme Variant:** Purple accents (#5C5FC4)

**Visual Elements:**
- **Header Section:**
  - Circular avatar (42px) with purple background (#5C5FC4), letter "W"
  - Title: "NexusChat Workstation" in white bold
  - Status: "● offline" in red (#FF6B6B)
  - **Connect Button:** Purple background, text "Connect", enabled state
- **Chat Area:** Empty or showing previous session history
- **Input Area:**
  - Entry field present but inactive context
  - Send button: **disabled**

### 2. Connecting State
**File:** `client_connecting.png`

**Transient State (typically <2 seconds):**
- Connect button shows "..." (ellipsis)
- Button appears disabled (prevents double-click)
- Status still shows "● offline" (not yet updated)
- Background thread attempting TCP connection

### 3. Connected State
**File:** `client_connected.png`

**Successful Connection Result:**
- Status: "● online" in green (#51CF66)
- Connect button: Disabled, text "Connected", grayed appearance
- Send button: **Enabled**, purple accent
- System message: "Connected to 127.0.0.1:12345"
- Full interaction capability available

### 4. Connection Failed State
**File:** `client_connection_failed.png`

**Error Handling Display:**
- Status: "● offline" in red
- Connect button: Enabled, text "Retry" (allows re-attempt)
- System message: "Connection failed · is server running?"
- Input remains disabled until successful connection

### 5. Active Conversation View
**File:** `client_conversation.png`

**Message Layout:**
- **Outgoing (user's messages):**
  - Purple bubbles (#5C5FC4), right-aligned
  - White text, wrap at 230px
  - Timestamp in purple-gray (#9B98DC)
- **Incoming (server messages):**
  - Dark gray bubbles (#25262B), left-aligned
  - Light text (#EAECEE)
  - Timestamp in gray (#7A8896)
- Auto-scroll maintains newest message visibility

### 6. Unexpected Disconnection
**File:** `client_disconnected.png`

**Handling Network Loss:**
- Status: "● offline" in red
- Connect button: Enabled, text "Reconnect"
- Send button: Disabled
- System message: "Disconnected from server."
- Chat history preserved for context

## UI Dimension Reference

| Element | Size | Position |
|---------|------|----------|
| Window | 420×680 px | Centered on screen |
| Header | 62 px height | Top, full width |
| Avatar | 42×42 px | Header left, 14px padding |
| Chat Area | Flexible | Between header/input |
| Input Area | 62 px height | Bottom, full width |
| Entry Field | 42 px height | Inside input area |
| Send Button | 42×42 px circular | Input area right |
| Bubble Max Width | 230 px | Word wrap boundary |
| Bubble Radius | 12 px | Rounded corners |

## Color Palette Reference

### Server Control Center Colors
```
Background Primary:    #17212B
Background Secondary:  #0E1621
Input Background:      #242F3D
Outgoing Bubble:       #2B5278
Incoming Bubble:       #182533
Accent Blue:           #3A6B9F
Status Online:         #51CF66
Status Waiting:        #FEE75C
System Messages:       #3D5A73
```

### Client Workstation Colors
```
Background Primary:    #1A1B1E
Background Secondary:  #141517
Input Background:      #25262B
Outgoing Bubble:       #5C5FC4
Incoming Bubble:       #25262B
Accent Hover:          #4A4DAA
Accent Client:         #5C5FC4
Status Online:         #51CF66
Status Offline:        #FF6B6B
System Messages:       #3D3D4A
```

---

*Screenshots should be captured at 2x resolution (840×1360px) for retina-quality documentation.*
