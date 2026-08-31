# Contributing to Blukit

We love your input! Blukit is built for offline use, not the web. We are building a future where communication is physical, local, and preserved on your own device.

## Design Philosophy: Local Network UI
Blukit follows a "Zero Configuration" philosophy. All complex background operations (Eco Mode, Stealth Mode, Radio Throttling) must be autonomous and hidden from the user.

### UI Guidelines
- **Simple Terminology**: Strictly avoid "tactical" or "mesh" terminology. Use "Chat", "People", "Share", and "Authenticity" over "Message", "Peer", "Broadcast", and "Consensus".
- **Background Complexity**: If a feature requires user configuration to work, it's not ready for Blukit. AI should handle optimizations silently.
- **Visuals**: Use Amber (`#FFB300`) and Rose (`#FF4081`) as primary theme colors against Black (`#030507`) for OLED efficiency.
- **Case Sensitivity**: Avoid all-caps in headers and labels. Use standard sentence casing with `HeadlineSmall` or `TitleMedium`.

## Agent Guidelines
AI Agents working on Blukit must:
1.  **Enforce Terminology**: Never suggest UI changes that use technical jargon.
2.  **Autonomous-First**: Prioritize silent, battery-aware, and activity-aware background logic over manual toggles.
3.  **Local-Only**: Ensure all intelligence and persistence logic remains 100% offline and hardware-anchored.

## Technical Verification
Contributors must verify changes against the following baseline:
1. **Device Fleet**: Testing on at least two physical devices (API 26+) is required to verify P2P handshakes and radio stability.
2. **Architecture**: Ensure new components are integrated into the modular UI structure (`RadarComponents`, `PersonaComponents`, etc.).
3. **Data Integrity**: Any changes to the message pipeline must respect the LWW-CRDT harmonization logic.
4. **Performance**: Verify that visual effects do not exceed 10% CPU usage on a mid-range device.

## We Use [Github Flow](https://guides.github.com/introduction/flow/), So All Code Changes Happen Through Pull Requests

1. Fork the repo and create your branch from `main`.
2. If you've added code that should be tested, add tests.
3. If you've changed APIs, update the documentation.
4. **Verification**: Always verify changes against a physical device fleet.
5. Ensure the test suite passes.
6. Make sure your code lints.
7. Issue that pull request!

## License
By contributing, you agree that your contributions will be licensed under its Apache 2.0 License.
