import { defineConfig } from "vitepress";

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "translator-bot",
  description: "A simple Discord Bot that will translate the world",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: "ToC", link: "/terms-of-service" },
      {
        text: "Privacy Policy",
        link: "/privacy-policy",
      },
      { text: "About", link: "/about" },
    ],
    search: {
      provider: "local",
    },
    sidebar: [{ text: "Install", link: "/install" }],
    lastUpdatedText: "Last updated",
    editLink: {
      pattern: "https://github.com/vitalegi/translator-bot/edit/master/:path",
      text: "Edit this page on GitHub",
    },
    socialLinks: [
      { icon: "github", link: "https://github.com/vitalegi/translator-bot" },
    ],
    docFooter: {
      prev: "Prev",
      next: "Next",
    },
  },
  lastUpdated: true,
  srcExclude: ["README.md"],
});
