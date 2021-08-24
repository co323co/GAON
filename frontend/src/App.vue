<template>
  <router-view></router-view>
</template>

<script>
export default {
  name: "App",

  components: {},
  data: () => ({
    //
  })
};
</script>
<style>
@import "https://unpkg.com/element-plus/theme-chalk/index.css";
@import "./views/main/main.css";
@import "./common/css/common.css";
@import "./common/css/element-plus.css";

.el-main {
  height: 100%;
  width: 100% !important;
  text-align: center;
}
</style>
