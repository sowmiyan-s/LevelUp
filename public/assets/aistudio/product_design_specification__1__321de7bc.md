# Product Design & UI/UX Specification

**Application:** Level Up — Daily Work Logger & Focus Shield

**Theme:** Emerald Gradient & Modern Bio-Tech Dark/Light Aesthetic

**Document Version:** 1.1

## 1. Design Vision & Visual Identity

Level Up is built around the philosophy of **Flow & Clarity**. The visual language uses deep emerald greens, mint highlights, and subtle glassmorphism to foster deep concentration without visual fatigue.

### Core Visual Pillars

* **Organic Energy:** Rich emerald and neon-mint gradients signal productivity and active growth.

* **Low Friction & High Focus:** High-contrast focal points guide the eye immediately to active tasks and the central Focus Ring.

* **Tactile Feedback:** Smooth micro-animations, glowing gradient rings, and precise dynamic status indicators.

## 2. Color System & Gradient Matrix

### 2.1 Color Tokens

| Token Name | Hex Code | Purpose | 
| ----- | ----- | ----- | 
| `Emerald-900` (Deep Base) | `#062E21` | Dark mode surface background / Header base | 
| `Emerald-700` (Forest Accent) | `#0E5E43` | Structural cards, unselected states | 
| `Emerald-500` (Core Brand) | `#10B981` | Primary action buttons, active toggles | 
| `Mint-400` (Neon Accent) | `#34D399` | Glow effects, active Focus Ring progress | 
| `Mint-200` (Highlighter) | `#A7F3D0` | Primary text highlights, badge backgrounds | 
| `Amber-500` (Warning/Partial) | `#F59E0B` | Partial goal completion indicator (50%–99%) | 
| `Crimson-500` (Missed/Locked) | `#EF4444` | Unfulfilled goals (<50%), active app lock shield | 
| `Gold-400` (Streak / Gold Day) | `#FBBF24` | 100% + Optional goal completion, active streak flame | 

### 2.2 Gradient Specifications

```
/* Core Focus Dial Ring Gradient */
--gradient-focus-ring: linear-gradient(135deg, #10B981 0%, #34D399 50%, #059669 100%);

/* Background Glow Effect */
--gradient-hero-glow: radial-gradient(circle at top center, rgba(52, 211, 153, 0.15) 0%, rgba(6, 46, 33, 0) 70%);

/* Focus Mode Active Shield Banner */
--gradient-shield-active: linear-gradient(90deg, #064E3B 0%, #047857 50%, #065F46 100%);

/* Perfect Day Gold Badge Gradient */
--gradient-gold-streak: linear-gradient(135deg, #FBBF24 0%, #F59E0B 100%);

```

## 3. Typography System

The typography relies on clean sans-serif typefaces optimized for legibility across tight timelines and large timer displays.

* **Primary Font Family:** `Inter` / `SF Pro Display`

* **Monospace Font (Timers & Code Logs):** `JetBrains Mono` / `Fira Code`

```
Display Large (Focus Timer):  56px | Weight: 700 (Bold)   | Letter-Spacing: -0.02em
Heading 1 (Screen Headers):   28px | Weight: 700 (Bold)   | Letter-Spacing: -0.01em
Heading 2 (Card Titles):      20px | Weight: 600 (Semi)   | Letter-Spacing: 0em
Body Regular (Log Notes):     15px | Weight: 400 (Normal) | Line-Height: 1.5
Caption/Badge (Time Stamps):  12px | Weight: 600 (Semi)   | Text-Transform: UPPERCASE

```

## 4. Flexible Iconography System

We use a modular, vector-based icon set (compatible with **Lucide Icons** / **Feather Icons**). Icons adapt dynamically in color based on active state and container background.

```
          ┌───────────────────────────────────────────────┐
          │               FLEXIBLE ICON MAP               │
          └───────────────────────┬───────────────────────┘
                                  │
      ┌───────────────────────────┼───────────────────────────┐
      ▼                           ▼                           ▼
[ NAVIGATION ]              [ STATUS ]                  [ ACTIONS ]
• Home: `layout-dashboard`  • Complete: `check-circle-2`    • Play: `play-circle`
• Log: `clock`              • Partial: `alert-triangle`     • Pause: `pause-circle`
• Calendar: `calendar`      • Missed: `x-circle`            • Stop & Save: `square`
• Analytics: `flame`        • Perfect: `award`              • Lock Shield: `shield-check`
• Settings: `sliders`       • Streak: `zap`                 • Add Log: `plus-circle`

```

## 5. Core UI Component Specifications

### 5.1 Interactive Focus Ring (Home Screen Dial)

The central timer dial is the heart of the app. It supports intuitive drag-to-adjust, single-tap toggling, and live visual feedback.

```
                  ┌───────────────────────────────┐
                  │       OUTER GLOW RING         │
                  │   ┌───────────────────────┐   │
                  │   │   PROGRESS TRACK      │   │
                  │   │   ┌───────────────┐   │   │
                  │   │   │  02:45:18     │   │   │
                  │   │   │  FOCUS MODE   │   │   │
                  │   │   │  [ ■ STOP ]   │   │   │
                  │   │   └───────────────┘   │   │
                  │   └───────────────────────┘   │
                  └───────────────────────────────┘

```

#### States & Behavioral Specs

1. **Idle State:**

   * Outer Track: Solid `Emerald-900` with 2px stroke (`Emerald-700`).

   * Inner Text: Displays cumulative time spent today vs target (e.g., `3h 45m / 6h Target`).

   * Main Button: Vibrant `Emerald-500` fill with white text **"START STUDY MODE"**.

2. **Active State (Timer Running):**

   * Outer Track: Animated multi-color gradient border (`--gradient-focus-ring`) sweeping clockwise.

   * Glow: Soft 12px outer blur shadow using `#34D399` at 30% opacity.

   * Sub-display: Animated pulsing dot `● FOCUS SHIELD ACTIVE`.

   * Action Controls: Dual buttons **\[ ⏸ PAUSE \]** and **\[ ⏹ STOP & LOG \]**.

### 5.2 Globalized Calendar View (4-Tier Status System)

The monthly calendar provides immediate macro-level feedback on consistency through dynamic color coding.

```
┌─────────────────────────────────────────────────────────────────────────┐
│ 📅 CALENDAR VIEW - OCTOBER 2026                                         │
├─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────────┤
│   MON   │   TUE   │   WED   │   THU   │   FRI   │   SAT   │     SUN     │
├─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────────┤
│  01     │  02     │  03     │  04     │  05     │  06     │  07         │
│  [ 🟩 ] │  [ 🟩 ] │  [ 🟧 ] │  [ 🟩 ] │  [ 🟦 ] │  [ 🟥 ] │  [ 🟩 ]     │
└─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────────┘

```

#### Color Mapping Logic

$$
\text{Completion Ratio } (R) = \frac{\text{Completed Essential Goals}}{\text{Total Essential Goals}}
$$

* 🟦 **Gold / Sapphire (Super Day):** $R = 1.0$ AND All Optional Bonus Goals completed.

* 🟩 **Green (Target Achieved):** $R = 1.0$ (100% of Essential Daily Goals completed).

* 🟧 **Amber (Partial Progress):** $0.50 \le R < 1.0$ (Keeps user motivated, prevents streak drop).

* 🟥 **Red (Needs Attention):** $R < 0.50$ (Fails daily consistency threshold).

* ❄️ **Cyan (Streak Freeze Used):** Day protected by earned Streak Shield.

### 5.3 24-Hour Time-Block Timeline & Sub-Note Drawer

Each session logged through the timer dial or manual input maps automatically onto an hourly vertical timeline.

```
09:00 AM ─────────────────────────────────────────────────────────
         │ ⚡ FOCUS SESSION (2h 30m)
         │ Category: Deep Work / Software Architecture
         │ Tags: #python #database #design
         │ ┌─────────────────────────────────────────────────────┐
         │ │ Sub-Notes & Deliverables:                           │
         │ │ • Completed DB Schema for Calendar Module           │
         │ │ • Resolved async state bug on timer pause           │
         │ └─────────────────────────────────────────────────────┘
11:30 AM ─────────────────────────────────────────────────────────
         │ ☕ Break / Rest
12:00 PM ─────────────────────────────────────────────────────────

```

#### Timeline Features

* **Time Grid:** 24-hour vertical layout with interactive drag-and-drop adjusters.

* **Rich Markdown Notes:** Supports inline code blocks, bullet points, checklists, and quick file attachments per block.

* **Auto-Tagging:** Color-coded pill badges (`#Study`, `#Code`, `#Fitness`, `#Reading`).

### 5.4 System Distraction Shield & Focus Mode Overlay

When **Study Mode** is toggled, the App Shield triggers native system restrictions.

```
┌─────────────────────────────────────────────────────────┐
│ 🔒 FOCUS SHIELD ACTIVE                                  │
│                                                         │
│  The following apps are locked during your session:     │
│  [ Instagram ]  [ X / Twitter ]  [ YouTube ]            │
│                                                         │
│  Remaining Focus Time: 01:24:10                         │
│                                                         │
│  [ Emergency Unlock (Penalty: Streak Reset Warning) ]   │
└─────────────────────────────────────────────────────────┘

```

## 6. Full App Screen Layout Mockups

### Screen 1: Main Focus Dashboard

```
┌─────────────────────────────────────────────────────────┐
│ ≡ Level Up                       🔥 14 Days   [👤 Profile]│
├─────────────────────────────────────────────────────────┤
│                                                         │
│                   ┌─────────────────┐                   │
│                   │    01:45:00     │                   │
│                   │  STUDY MODE     │                   │
│                   │   [ ▶ START ]   │                   │
│                   └─────────────────┘                   │
│                      Goal: 4h / 6h                      │
│                                                         │
│ 🛡️ FOCUS SHIELD STATUS                                   │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ 🟢 Active Protection: 4 Apps Blocked               │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                         │
│ 🎯 TODAY'S ESSENTIAL GOALS                              │
│ [✓] 1. Complete Chapter 4 Algorithms (2h)              │
│ [ ] 2. System Architecture Design (2h)                 │
│ [ ] 3. Daily Workout & Hydration (1h)                  │
│                                                         │
├─────────────────────────────────────────────────────────┤
│  [🏠 Home]    [⏱ Timeline]    [📅 Calendar]    [📊 Stats]│
└─────────────────────────────────────────────────────────┘

```

## 7. Micro-Interactions, Haptics & Sound Design

To reinforce positive behavior loops, the app incorporates tactile and auditory responses:

1. **Timer Start (Focus Mode On):**

   * **Haptic:** Medium double pulse.

   * **Audio:** Soft low-frequency synthetic sweep rising upwards.

   * **Visual:** Background dims slightly; mint gradient ring pulses once and locks into place.

2. **Goal Check-off:**

   * **Haptic:** Light sharp tick (`HapticFeedbackType.lightImpact`).

   * **Visual:** Checkbox fills with `Mint-400` gradient; subtle particle burst animation.

3. **Perfect Day Achieved (Gold Status):**

   * **Visual:** Full-screen soft shimmer animation; Calendar tile explodes with gold glow.

   * **Haptic:** Heavy rhythmic triple pulse (`HapticFeedbackType.success`).

## 8. Theme Implementation Tokens (CSS / Tailwind Config)

```
module.exports = {
  theme: {
    extend: {
      colors: {
        emerald: {
          900: '#062E21',
          800: '#064E3B',
          700: '#0E5E43',
          500: '#10B981',
        },
        mint: {
          400: '#34D399',
          200: '#A7F3D0',
        },
        accent: {
          gold: '#FBBF24',
          amber: '#F59E0B',
          crimson: '#EF4444',
        }
      },
      backgroundImage: {
        'focus-ring': 'linear-gradient(135deg, #10B981 0%, #34D399 50%, #059669 100%)',
        'shield-active': 'linear-gradient(90deg, #064E3B 0%, #047857 50%, #065F46 100%)',
      }
    }
  }
}

```