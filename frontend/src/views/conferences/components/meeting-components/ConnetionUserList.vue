<template lang="">
  <el-card
    :body-style="{ padding: '10px' }"
    style="width: 100%; border-radius:3%"
  >
    <el-table :data="rollbookmemberList">
      <el-table-column label="접속현황" prop="nickname"> </el-table-column>
      <el-table-column width="60px">
        <template #default="scope">
          <div
            v-if="rollbookmemberList[scope.$index].state"
            style="background-color:#67C23A; width:13px; height:13px; border-radius: 50%"
            circle
          ></div>
          <div
            v-else
            style="background-color:#F56C6C; width:13px; height:13px; border-radius: 50%"
            circle
          ></div>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
<script>
import $axios from "axios";
export default {
  data() {
    return {
      rollbookmemberList: [],
      search: ""
    };
  },
  computed: {
    rollbookmemberList() {
      this.rollbookmemberList.forEach(rollbookmember => {
        var cnt = 0;
        const { idData } = this.getConnectionData(this.publisher);
        if (idData == rollbookmember.user_id) {
          console.log("나 자신");
          rollbookmember["state"] = true;
          cnt++;
        }
        this.subscribers.forEach(sub => {
          const { idData } = this.getConnectionData(sub);
          if (idData == rollbookmember.user_id) {
            console.log("들어온 사람");
            rollbookmember["state"] = true;
            cnt++;
          }
        });
        if (cnt == 0) {
          rollbookmember["state"] = false;
        }
      });
      return this.rollbookmemberList;
    }
  },
  props: {
    publisher: undefined,
    subscribers: []
  },
  async created() {
    // 기본이 state가
    this.rollbookmemberList = await this.$store.dispatch(
      "getMembersByUsingRoomId",
      this.$route.params.conferenceId
    );
    // state 기본은 결석으로 설정해주기
    this.rollbookmemberList.forEach(rollbookmember => {
      rollbookmember["state"] = "결석";
    });
    // this.rollbookmemberList = this.rollbookmemberList.filter(function(item) {
    //     item["state"] = "결석";
    //     return item;
    //   });
  },
  methods: {
    getConnectionData(subscriber) {
      const { connection } = subscriber.stream;
      return JSON.parse(connection.data);
    },
    createRollBook() {
      var today = new Date();
      var year = today.getFullYear();
      var month = ("0" + (today.getMonth() + 1)).slice(-2);
      var day = ("0" + today.getDate()).slice(-2);
      var dateString = year + "-" + month + "-" + day;
      const rollbooklist = [];
      this.rollbookmemberList.forEach(rollbookmember => {
        const rollbook = {
          date: dateString,
          rid: this.$route.params.conferenceId,
          state: rollbookmember["state"],
          uid: rollbookmember.user_id
        };
        rollbooklist.push(rollbook);
      });
      $axios
        .post("/rollbook/", rollbooklist)
        .then(res => {
          console.log("출석부 저장 성공");
          console.log(res.data);
        })
        .catch(err => {
          console.log(err);
        });
    }
  }
};
</script>
<style lang=""></style>
