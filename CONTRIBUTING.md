# Contributing to Blukit

We love your input! Blukit is built for offline use, not the web. We are building a future where communication is physical, local, and preserved on your own device.

## Design Philosophy: Local Network UI
Blukit follows a design system optimized for high-fidelity OLED performance and ergonomic density. All UI components should:
- Use Amber (`#FFB300`) and Rose (`#FF4081`) as the primary theme colors against Black (`#030507`) for OLED efficiency.
- Use `MaterialTheme.colorScheme.outlineVariant` (`#2D333B`) for critical borders to ensure high contrast.
- Use background glows, Activity Heatmaps, and visual connection indicators over static, standard Material components.
- Replace standard system dialogs with custom overlays or Blukit Alerts to maintain immersion.
- Enforce consistent typography: Avoid all-caps in headers, navigation, and labels. Use standard sentence casing with `HeadlineSmall` or `TitleMedium`.

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
