import Course from '@/models/user/Course';

interface CourseMap {
  [key: string]: Course[];
}

export default class User {
  id!: number;
  name!: string;
  username!: string;
  role!: string;
  courses: CourseMap = {};
  coursesNumber: number = 0;

  constructor(jsonObj?: User) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.name = jsonObj.name;
      this.username = jsonObj.username;
      this.role = jsonObj.role;

      for (let [name, courses] of Object.entries(jsonObj.courses)) {
        this.courses[name] = courses.map(course => new Course(course));
        this.coursesNumber += this.courses[name].length;
      }
    }
  }
}
