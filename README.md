# Gasodo

**Gasodo** is an offline-first Android application built with **Jetpack Compose** for tracking your
vehicle's refueling, maintenance, and inspection history. Manage all of your car's recurring costs
and health in one place.

## Goal & Purpose

The primary goal of this project is to provide a comprehensive mobile solution that lets car owners
log and analyze everything relevant to their vehicle's operation:

- **Track Refueling Events** — Log each refuel with liters pumped, price per liter, total cost, fuel
  type, payment method, full-tank flag, and optional saved location
- **Track Maintenance Events** — Record maintenance work (oil change, brake change, tire rotation,
  etc.) with the services performed and parts used
- **Track Inspection Events** — Log the outcome of vehicle inspections (pass / fail / conditional
  pass) per inspected area
- **Save Locations** — Keep a list of gas stations and service providers for quick references across
  events
- **Monitor Consumption** — Analyze fuel efficiency and cost statistics over time

## Features

### Core Functionality

- **Refueling** — Full fuel event logging with cost analysis
- **Maintenance** — Track maintenance work and associated service types (pre-seeded with common
  services)
- **Inspection** — Log inspection results per part (pre-seeded with common inspectable parts)
- **Saved Locations** — Reusable gas stations and service locations attached to events
- **Overview Dashboard** — Aggregate statistics, date-range filtering, and cost/mileage insights
- **Unified Add/Edit Event** — One interface to create or edit any event type with validation

### Technical Features

- Offline-first architecture with local Room database
- MVVM (Model-View-ViewModel) architectural pattern
- Hilt dependency injection
- Navigation Compose with a bottom navigation bar
- Paging 3 for efficient data loading

## Current Event Types

| Type            | Description                                   |
|-----------------|-----------------------------------------------|
| **Refueling**   | Fuel purchase details, cost, payment method   |
| **Maintenance** | Work performed, services used, parts replaced |
| **Inspection**  | Per-part inspection outcomes and status       |

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM
- **DI:** Hilt
- **Database:** Room (with KSP, exported schema)
- **Navigation:** Navigation Compose
- **Min SDK / Target SDK:** 33 / 36

## Future Enhancements

- Chart/graph visualization for fuel consumption trends
- Multi-vehicle support
- Cloud backup/sync capability?

## License

This project is licensed under the terms of the [LICENSE](LICENSE) file.