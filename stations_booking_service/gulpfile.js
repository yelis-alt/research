const scss = require('gulp-sass')(require('sass'));
const concat = require('gulp-concat');
const autoprefixer = require('gulp-autoprefixer');
const uglify = require('gulp-uglify');
const imagemin = require('gulp-imagemin');
const del = require('del');
const gulp = require("gulp");

function styles() {
    return gulp.src(['app/frontend/css/reset.scss',
		             'app/frontend/css/style.scss'])
    .pipe(scss({outputStyle: 'compressed'}))
    .pipe(concat('style.min.css'))
    .pipe(autoprefixer({
        overrideBrowserlist: ['last 10 versions'],
        grid: true
    }))
    .pipe(gulp.dest('app/frontend/css'))
}

function scripts() {
    return gulp.src([
        'node_modules/jquery/dist/jquery.js',
        'app/frontend/js/*.js',
        '!app/frontend/js/main.min.js'
    ])
    .pipe(concat('main.min.js'))
    .pipe(uglify())
    .pipe(gulp.dest('app/frontend/js'))
}

function images() {
    return gulp.src('app/frontend/images/**/*.*')
    .pipe(imagemin([imagemin.gifsicle({interlaced: true}),
	imagemin.mozjpeg({quality: 75, progressive: true}),
	imagemin.optipng({optimizationLevel: 5}),
	imagemin.svgo({
		plugins: [
			{removeViewBox: true},
			{cleanupIDs: false}
		]
	})]))
    .pipe(gulp.dest('dist/frontend/images'))
}

function build() {
   return gulp.src(['app/frontend/templates/main.php',
                    'app/backend/python/*.py',
                    'app/frontend/css/style.min.css',
                    'app/frontend/js/main.min.js',
                    'app/frontend/fonts/*.ttf'], {base:'app'})
       .pipe(gulp.dest('dist'))
}

function watching() {
    gulp.watch(['app/frontend/css/**/*.scss'], styles);
    gulp.watch(['app/frontend/js/**/*.js',
               '!app/frontend/js/main.min.js'], scripts);
}

function cleanDist(){
    return del('dist')
}

exports.styles = styles;
exports.scripts = scripts;
exports.watching = watching;
exports.images = images;
exports.build = build;
exports.cleanDist = cleanDist;

exports.compose = gulp.series(styles, scripts, build);

exports.build = gulp.series(cleanDist, images, build);

exports.watch = gulp.parallel(styles, scripts, watching);