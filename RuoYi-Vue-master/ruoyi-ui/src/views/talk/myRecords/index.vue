<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="$router.push('/talk/talkInitiate/index')">发起谈话</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="talksessionList">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column label="谈话类型" align="center" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.talkType === 'individual'" type="primary" size="small">个别</el-tag>
          <el-tag v-else type="success" size="small">集体</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="谈话时间" align="center" width="120">
        <template slot-scope="scope">{{ scope.row.talkTime ? scope.row.talkTime.substring(0,10) : '' }}</template>
      </el-table-column>
      <el-table-column label="谈话地点" prop="talkLocation" min-width="120" align="center" show-overflow-tooltip />
      <el-table-column label="谈话内容" prop="talkContent" min-width="200" align="center" show-overflow-tooltip />
      <el-table-column label="参与学生" min-width="120" align="center">
        <template slot-scope="scope">
          <span v-if="studentMap[scope.row.sessionId]">
            {{ studentMap[scope.row.sessionId].join('、') }}
          </span>
          <span v-else style="color:#ccc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="标签" width="100" align="center">
        <template slot-scope="scope">
          <span v-if="tagMap[scope.row.sessionId]" style="font-size:12px;color:#666">
            {{ tagMap[scope.row.sessionId].map(t => getTagLabel(t.tagValue)).join('、') || '-' }}
          </span>
          <span v-else style="color:#ccc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-download" @click="handleExport(scope.row)">导出</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listTalksession, getSessionTags, TAG_LABELS } from '@/api/talk/talkSession'
import request from '@/utils/request'
import { listTalkrecord } from '@/api/talk/talkStudentRecord'
import { getTalk } from '@/api/talk/talkStudent'

export default {
  name: 'MyRecords',
  data() {
    return {
      loading: false,
      showSearch: false,
      total: 0,
      talksessionList: [],
      tagMap: {},
      studentMap: {},
      queryParams: { pageNum: 1, pageSize: 10 }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listTalksession(this.queryParams).then(res => {
        this.talksessionList = res.rows || []
        this.total = res.total
        this.loadTags()
        this.loadStudents()
      }).finally(() => { this.loading = false })
    },
    loadTags() {
      this.talksessionList.forEach(s => {
        getSessionTags(s.sessionId).then(r => this.$set(this.tagMap, s.sessionId, r.data || []))
      })
    },
    loadStudents() {
      this.talksessionList.forEach(s => {
        listTalkrecord({ sessionId: s.sessionId, pageSize: 999 }).then(r => {
          const ids = (r.rows || []).map(rec => rec.studentId)
          Promise.all(ids.map(id => getTalk(id))).then(students => {
            this.$set(this.studentMap, s.sessionId, students.map(st => st.data.studentName))
          })
        })
      })
    },
    getTagLabel(v) { return TAG_LABELS[v] || v },
    handleExport(row) {
      const ext = row.talkType === 'group' ? '.zip' : '.docx'
      this.$modal.confirm('导出' + row.talkPerson + '的谈话记录？').then(() => {
        return request({ url: '/ruoyi-system/talksession/exportDocx/' + row.sessionId, method: 'get', responseType: 'blob' })
      }).then(blob => {
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a'); a.href = url
        a.download = '谈话记录_' + (row.talkPerson || row.sessionId) + ext
        a.click(); window.URL.revokeObjectURL(url)
        this.$modal.msgSuccess('导出成功')
      }).catch(() => {})
    }
  }
}
</script>
