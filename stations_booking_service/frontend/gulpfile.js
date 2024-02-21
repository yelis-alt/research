const scss = require('gulp-sass')(require('sass'));
const concat = require('gulp-concat');
const autoprefixer = require('gulp-autoprefixer');
const uglify = require('gulp-uglify');
const imagemin = require('gulp-imagemin');
const del = require('del');
const gulp = require("gulp");

function styles() {
    return gulp.src(['app/css/reset.scss',
		             'app/css/style.scss'])
    .pipe(scss({outputStyle: 'compressed'}))
    .pipe(concat('style.min.css'))
    .pipe(autoprefixer({
        overrideBrowserlist: ['last 10 versions'],
        grid: true
    }))
    .pipe(gulp.dest('app/css'))
}

function scripts() {
    return gulp.src([
        'node_modules/jquery/dist/jquery.js',
        'app/js/*.js',
        '!app/js/main.min.js'
    ])
    .pipe(concat('main.min.js'))
    .pipe(uglify())
    .pipe(gulp.dest('app/js'))
}

function images() {
    return gulp.src('app/images/**/*.*')
    .pipe(imagemin([
	imagemin.mozjpeg({quality: 75, progressive: true}),
	imagemin.svgo({
		plugins: [
			{removeViewBox: true},
			{cleanupIDs: false}
		]
	})]))
    .pipe(gulp.dest('dist/images'))
}

function build() {
   return gulp.src(['app/index.html',
                    'app/css/style.min.css',
                    'app/js/main.min.js',
                    'app/fonts/*.ttf'], {base:'app'})
       .pipe(gulp.dest('dist'))
}

function watching() {
    gulp.watch(['app/css/**/*.scss'], styles);
    gulp.watch(['app/js/**/*.js',
               '!app/js/main.min.js'], scripts);
}

function cleanDist(){
    return del('dist')
}

exports.styles = styles;
exports.scripts = scripts;
exports.watching = watching;
exports.images = images;
exports.cleanDist = cleanDist;

exports.compose = gulp.series(styles, scripts, build);

exports.build = gulp.series(styles, scripts, cleanDist, images, build);

exports.watch = gulp.parallel(styles, scripts, watching);