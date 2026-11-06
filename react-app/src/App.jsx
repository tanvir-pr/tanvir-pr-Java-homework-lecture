import { useEffect, useState } from 'react'
import axios from 'axios'

const emptyForm = { id: null, name: '', department: '', email: '', salary: '' }

export default function App() {
  const [employees, setEmployees] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      const { data } = await axios.get('/api/employees')
      setEmployees(data)
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load employees')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const onChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const onSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const payload = {
      name: form.name.trim(),
      department: form.department.trim(),
      email: form.email.trim(),
      salary: Number(form.salary)
    }
    try {
      if (form.id) {
        await axios.put(`/api/employees/${form.id}`, payload)
      } else {
        await axios.post('/api/employees', payload)
      }
      setForm(emptyForm)
      await load()
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Save failed')
    }
  }

  const onEdit = (employee) => {
    setForm({
      id: employee.id,
      name: employee.name,
      department: employee.department,
      email: employee.email,
      salary: String(employee.salary)
    })
  }

  const onDelete = async (id) => {
    if (!window.confirm('Delete this employee?')) {
      return
    }
    setError('')
    try {
      await axios.delete(`/api/employees/${id}`)
      if (form.id === id) {
        setForm(emptyForm)
      }
      await load()
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Delete failed')
    }
  }

  return (
    <div>
      <h1>Employee CRUD</h1>
      <p>React + Axios talking to Spring Boot REST API (H2 file DB).</p>

      {error && <div className="error">{error}</div>}

      <div className="panel">
        <h2>{form.id ? `Edit #${form.id}` : 'Create employee'}</h2>
        <form onSubmit={onSubmit}>
          <label>
            Name
            <input name="name" value={form.name} onChange={onChange} required />
          </label>
          <label>
            Department
            <input name="department" value={form.department} onChange={onChange} required />
          </label>
          <label>
            Email
            <input type="email" name="email" value={form.email} onChange={onChange} required />
          </label>
          <label>
            Salary
            <input type="number" step="0.01" min="0" name="salary" value={form.salary} onChange={onChange} required />
          </label>
          <button type="submit">{form.id ? 'Update' : 'Create'}</button>
          {form.id && (
            <button type="button" className="secondary" onClick={() => setForm(emptyForm)}>
              Cancel edit
            </button>
          )}
        </form>
      </div>

      <div className="panel">
        <h2>Employees {loading ? '(loading...)' : `(${employees.length})`}</h2>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Department</th>
              <th>Email</th>
              <th>Salary</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {employees.map((employee) => (
              <tr key={employee.id}>
                <td>{employee.id}</td>
                <td>{employee.name}</td>
                <td>{employee.department}</td>
                <td>{employee.email}</td>
                <td>{employee.salary}</td>
                <td className="actions">
                  <button type="button" className="secondary" onClick={() => onEdit(employee)}>Edit</button>
                  <button type="button" className="danger" onClick={() => onDelete(employee.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
