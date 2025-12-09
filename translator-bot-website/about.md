---
layout: page
---

<script setup>
import {
  VPTeamPage,
  VPTeamPageTitle,
  VPTeamMembers
} from 'vitepress/theme'

const members = [
  {
    avatar: '/princimicio.jpg',
    name: 'Pinco Princi Puccio Micio',
    title: 'Team Leader',
    links: [
      { icon: 'instagram', link: 'https://www.instagram.com/orione.russianblue' },
    ]
  },
  {
    avatar: 'https://avatars.githubusercontent.com/u/6169537',
    name: 'Giorgio Vitale',
    title: 'Helper',
    links: [
      { icon: 'github', link: 'https://github.com/vitalegi' },
      { icon: 'instagram', link: 'https://www.instagram.com/giorgio.bellavita.vitale' }
    ]
  }
]
</script>

<VPTeamPage>
  <VPTeamPageTitle>
    <template #title>
      Il nostro Team
    </template>
    <template #lead>
      <i>Health</i> è sviluppato e gestito dal nostro team di esperti, Leader di un sacco di settori.
    </template>
  </VPTeamPageTitle>
  <VPTeamMembers
    :members="members"
  />
</VPTeamPage>